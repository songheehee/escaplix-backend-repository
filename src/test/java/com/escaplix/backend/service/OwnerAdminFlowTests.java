package com.escaplix.backend.service;

import com.escaplix.backend.common.auth.JwtProvider;
import com.escaplix.backend.entity.Area;
import com.escaplix.backend.entity.Genre;
import com.escaplix.backend.entity.Member;
import com.escaplix.backend.entity.Region;
import com.escaplix.backend.entity.Role;
import com.escaplix.backend.repository.AreaRepository;
import com.escaplix.backend.repository.GenreRepository;
import com.escaplix.backend.repository.MemberRepository;
import com.escaplix.backend.repository.RegionRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * C 담당 API (C-1 ~ C-19) 흐름 확인. H2 에서 돈다 (Oracle 트리거·CHECK 는 확인하지 않음).
 * 사장 매장 등록 → 관리자 반려 → 수정 → 승인 → 테마 등록 → 시간표 → 테마 승인 → 운영 종료 순서.
 */
@SpringBootTest
class OwnerAdminFlowTests {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwtProvider;
    @Autowired MemberRepository memberRepository;
    @Autowired GenreRepository genreRepository;
    @Autowired RegionRepository regionRepository;
    @Autowired AreaRepository areaRepository;

    MockMvc mvc;
    String tag;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
        saveArea("홍대");   // 매장 등록·수정은 AREA 표에 있는 지역만 받음 (결정 L1)
    }

    /** 세부 지역이 없으면 넣는다 (H2 는 테스트끼리 데이터를 같이 씀) */
    private void saveArea(String name) {
        if (areaRepository.existsByName(name)) return;
        Region r = new Region();
        r.setName("테스트지역" + tag);
        r.setSortOrder(1);
        regionRepository.save(r);
        Area a = new Area();
        a.setRegion(r);
        a.setName(name);
        a.setSortOrder(1);
        areaRepository.save(a);
    }

    @Test
    void 사장_매장_테마_시간표와_관리자_승인_흐름() throws Exception {
        String owner1 = bearer(saveOwner("own1"));
        String owner2 = bearer(saveOwner("own2"));
        String admin = "Bearer " + jwtProvider.create(9999L, "관리자", Role.ADMIN);
        long horror = saveGenre("공포");
        long mystery = saveGenre("추리");
        String bizPlaceNo = "123-45-" + tag;

        // C-3 목록에 없는 지역은 400
        mvc.perform(storeForm(multipart("/api/owner/stores"), bizPlaceNo, "없는동네")
                        .file(new MockMultipartFile("bizPlaceFile", "place.png", "image/png", PNG))
                        .header("Authorization", owner1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("지역은 목록에서 골라 주세요."));

        // C-3 사장은 사업장등록증 필수
        mvc.perform(storeForm(multipart("/api/owner/stores"), bizPlaceNo).header("Authorization", owner1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사업장등록증 이미지를 첨부해 주세요."));

        // C-3 등록 (매장 사진 포함) → PENDING
        MvcResult created = mvc.perform(storeForm(multipart("/api/owner/stores"), bizPlaceNo)
                        .file(new MockMultipartFile("bizPlaceFile", "place.png", "image/png", PNG))
                        .file(new MockMultipartFile("storePhoto", "photo.png", "image/png", PNG))
                        .header("Authorization", owner1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        long storeId = idOf(created, "$.storeId");

        // C-2 중복 확인
        mvc.perform(get("/api/owner/stores/check-biz-place-no").param("bizPlaceNo", bizPlaceNo).header("Authorization", owner1))
                .andExpect(jsonPath("$.available").value(false));

        // C-7 승인 전 매장에는 테마 등록 불가
        mvc.perform(themeForm(multipart("/api/owner/themes"), storeId, horror).header("Authorization", owner1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("승인된 매장에만 테마를 등록할 수 있습니다."));

        // C-16 반려는 사유 필수 → 반려 → 사장 홈(C-1)에 사유
        mvc.perform(json(put("/api/admin/stores/" + storeId + "/approval"), "{\"approved\":false}").header("Authorization", admin))
                .andExpect(status().isBadRequest());
        mvc.perform(json(put("/api/admin/stores/" + storeId + "/approval"), "{\"approved\":false,\"reason\":\"상호가 다릅니다.\"}").header("Authorization", admin))
                .andExpect(jsonPath("$.status").value("REJECTED"));
        mvc.perform(get("/api/owner/stores").header("Authorization", owner1))
                .andExpect(jsonPath("$[0].rejectReason").value("상호가 다릅니다."))
                .andExpect(jsonPath("$[0].photoUrl").value(org.hamcrest.Matchers.startsWith("/uploads/stores/")));
        mvc.perform(json(put("/api/admin/stores/" + storeId + "/approval"), "{\"approved\":true}").header("Authorization", admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 처리된 매장입니다."));

        // C-4 남의 매장 403, 내 매장 수정 → 다시 PENDING
        mvc.perform(storeForm(multipart(HttpMethod.PUT, "/api/owner/stores/" + storeId), bizPlaceNo).header("Authorization", owner2))
                .andExpect(status().isForbidden());
        mvc.perform(storeForm(multipart(HttpMethod.PUT, "/api/owner/stores/" + storeId), bizPlaceNo).header("Authorization", owner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // C-13 · C-14 · C-15 관리자 승인 화면
        mvc.perform(get("/api/admin/stores").header("Authorization", admin))
                .andExpect(jsonPath("$[*].storeId", hasItem((int) storeId)));
        mvc.perform(get("/api/admin/stores/" + storeId).header("Authorization", admin))
                .andExpect(jsonPath("$.hasBizPlaceFile").value(true))
                .andExpect(jsonPath("$.photoUrl").exists())   // C-4 에서 사진을 비웠으니 기존 사진 유지
                .andExpect(jsonPath("$.owner.bizNo").exists());
        mvc.perform(get("/api/admin/stores/" + storeId + "/biz-place-file").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        // C-16 승인
        mvc.perform(json(put("/api/admin/stores/" + storeId + "/approval"), "{\"approved\":true}").header("Authorization", admin))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.rejectReason").doesNotExist());

        // C-7 인원 검사, 등록 → PENDING
        mvc.perform(themeForm(multipart("/api/owner/themes"), storeId, 5, 2, horror).header("Authorization", owner1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("최소 인원이 최대 인원보다 클 수 없습니다."));
        MvcResult themeCreated = mvc.perform(themeForm(multipart("/api/owner/themes"), storeId, horror, mystery)
                        .file(new MockMultipartFile("poster", "p.png", "image/png", PNG))
                        .header("Authorization", owner1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        long themeId = idOf(themeCreated, "$.themeId");

        // C-11 시간표 검사 → 저장 → 다시 저장(통째 교체)
        mvc.perform(json(put("/api/owner/themes/" + themeId + "/slots"), "{\"weekday\":[\"13:00\",\"13:30\"],\"weekend\":[]}").header("Authorization", owner1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("주중 13:00과 13:30 회차가 겹칩니다 (플레이 70분)."));
        mvc.perform(json(put("/api/owner/themes/" + themeId + "/slots"), "{\"weekday\":[\"15:00\",\"15:00\"],\"weekend\":[]}").header("Authorization", owner1))
                .andExpect(jsonPath("$.message").value("주중 15:00이 두 번 들어 있습니다."));
        mvc.perform(json(put("/api/owner/themes/" + themeId + "/slots"), "{\"weekday\":[\"9:00\"],\"weekend\":[]}").header("Authorization", owner1))
                .andExpect(jsonPath("$.message").value("시간은 19:00처럼 입력해 주세요."));
        String slots = "{\"weekday\":[\"15:00\",\"13:00\"],\"weekend\":[\"10:00\"]}";
        mvc.perform(json(put("/api/owner/themes/" + themeId + "/slots"), slots).header("Authorization", owner1))
                .andExpect(status().isOk());
        mvc.perform(json(put("/api/owner/themes/" + themeId + "/slots"), slots).header("Authorization", owner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekday[0].startTime").value("13:00"))
                .andExpect(jsonPath("$.weekday[0].endTime").value("14:10"))
                .andExpect(jsonPath("$.weekend.length()").value(1));
        // C-10 남의 테마 403
        mvc.perform(get("/api/owner/themes/" + themeId + "/slots").header("Authorization", owner2))
                .andExpect(status().isForbidden());

        // C-9 승인 전에는 운영 상태 못 바꿈
        mvc.perform(json(put("/api/owner/themes/" + themeId + "/status"), "{\"status\":\"CLOSED\"}").header("Authorization", owner1))
                .andExpect(status().isConflict());

        // C-17 · C-18 · C-19 테마 승인
        mvc.perform(get("/api/admin/themes").header("Authorization", admin))
                .andExpect(jsonPath("$[*].themeId", hasItem((int) themeId)));
        mvc.perform(get("/api/admin/themes/" + themeId).header("Authorization", admin))
                .andExpect(jsonPath("$.theme.genres.length()").value(2))
                .andExpect(jsonPath("$.store.status").value("APPROVED"));
        mvc.perform(json(put("/api/admin/themes/" + themeId + "/approval"), "{\"approved\":true}").header("Authorization", admin))
                .andExpect(jsonPath("$.status").value("OPEN"));

        // C-9 운영 종료
        mvc.perform(json(put("/api/owner/themes/" + themeId + "/status"), "{\"status\":\"CLOSED\"}").header("Authorization", owner1))
                .andExpect(jsonPath("$.status").value("CLOSED"));

        // C-5 · C-6
        mvc.perform(get("/api/owner/stores/" + storeId + "/themes").header("Authorization", owner1))
                .andExpect(jsonPath("$[0].status").value("CLOSED"))
                .andExpect(jsonPath("$[0].reviewCount").value(0));
        mvc.perform(get("/api/owner/themes/" + themeId).header("Authorization", owner1))
                .andExpect(jsonPath("$.genreIds.length()").value(2))
                .andExpect(jsonPath("$.source").value("OWNER"));

        // C-8 사장이 수정하면 다시 PENDING, 장르 교체
        mvc.perform(themeForm(multipart(HttpMethod.PUT, "/api/owner/themes/" + themeId), storeId, mystery).header("Authorization", owner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(get("/api/owner/themes/" + themeId).header("Authorization", owner1))
                .andExpect(jsonPath("$.genreIds.length()").value(1));

        // 관리자 직접 등록: 파일 없이 바로 APPROVED, 사장 매장은 관리자가 수정 못 함
        mvc.perform(storeForm(multipart("/api/owner/stores"), "987-65-" + tag).header("Authorization", admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(storeForm(multipart(HttpMethod.PUT, "/api/owner/stores/" + storeId), bizPlaceNo).header("Authorization", admin))
                .andExpect(status().isForbidden());

        // C-12 요약
        mvc.perform(get("/api/admin/summary").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingThemes").isNumber());
    }

    // ------------------------------------------------------------------

    private Member saveOwner(String name) {
        Member m = new Member();
        m.setLoginId(name + tag);
        m.setPassword("x");
        m.setNickname(name + tag);
        m.setRole(Role.OWNER);
        m.setBizNo("111-22-" + tag);
        m.setBizFilePath("biz/test.png");
        return memberRepository.save(m);
    }

    private long saveGenre(String name) {
        Genre g = new Genre();
        g.setName(name + tag);
        return genreRepository.save(g).getGenreId();
    }

    private String bearer(Member m) {
        return "Bearer " + jwtProvider.create(m.getMemberId(), m.getNickname(), m.getRole());
    }

    private static MockMultipartHttpServletRequestBuilder storeForm(MockMultipartHttpServletRequestBuilder b, String bizPlaceNo) {
        return storeForm(b, bizPlaceNo, "홍대");
    }

    private static MockMultipartHttpServletRequestBuilder storeForm(MockMultipartHttpServletRequestBuilder b, String bizPlaceNo, String area) {
        return (MockMultipartHttpServletRequestBuilder) b
                .param("name", "테스트 매장")
                .param("area", area)
                .param("address", "서울 마포구")
                .param("phone", "02-000-0001")
                .param("websiteUrl", "https://example.com")
                .param("hours", "10:00 ~ 23:00")
                .param("intro", "소개")
                .param("bizPlaceNo", bizPlaceNo);
    }

    private static MockMultipartHttpServletRequestBuilder themeForm(MockMultipartHttpServletRequestBuilder b, long storeId, long... genreIds) {
        return themeForm(b, storeId, 2, 4, genreIds);
    }

    private static MockMultipartHttpServletRequestBuilder themeForm(MockMultipartHttpServletRequestBuilder b, long storeId,
                                                                    int minPeople, int maxPeople, long... genreIds) {
        String[] ids = java.util.Arrays.stream(genreIds).mapToObj(String::valueOf).toArray(String[]::new);
        return (MockMultipartHttpServletRequestBuilder) b
                .param("storeId", String.valueOf(storeId))
                .param("title", "테스트 테마")
                .param("difficulty", "3")
                .param("playMinutes", "70")
                .param("minPeople", String.valueOf(minPeople))
                .param("maxPeople", String.valueOf(maxPeople))
                .param("price", "25000")
                .param("bookingUrl", "https://example.com/booking")
                .param("genreIds", ids);
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder json(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder b, String body) {
        return b.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static long idOf(MvcResult r, String path) throws Exception {
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), path)).longValue();
    }
}
