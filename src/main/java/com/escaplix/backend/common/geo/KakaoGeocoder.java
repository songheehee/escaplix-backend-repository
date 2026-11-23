package com.escaplix.backend.common.geo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 주소 → 좌표 (카카오 로컬 API "주소 검색"). 매장 등록·수정(C-3·C-4) 때 lat·lng 를 채운다.
 * - 키: application-local.properties 의 kakao.rest-key (git 에 안 올라감). 비어 있으면 아무것도 안 하고 빈 값
 * - 실패(키 없음, 결과 없음, 네트워크 오류, 3초 넘음)하면 빈 값 → 좌표 없이 저장하고 매장 상세 지도만 안 보임
 * - 주소는 "도로명 주소, 상세 주소" 모양으로 들어온다. 상세 주소(쉼표 뒤)는 검색에서 뺀다
 */
@Slf4j
@Component
public class KakaoGeocoder {

    private static final String URL = "https://dapi.kakao.com/v2/local/search/address.json";

    private final String restKey;
    private final RestClient client;

    public KakaoGeocoder(@Value("${kakao.rest-key:}") String restKey) {
        this.restKey = restKey == null ? "" : restKey.trim();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    /** 위도(lat)·경도(lng) */
    public record LatLng(BigDecimal lat, BigDecimal lng) {}

    /** 카카오 응답에서 쓰는 칸만. x = 경도, y = 위도 */
    record Response(List<Doc> documents) {}
    record Doc(String x, String y) {}

    public Optional<LatLng> find(String address) {
        if (restKey.isEmpty() || restKey.startsWith("키") || address == null || address.isBlank()) {
            return Optional.empty();
        }
        String query = address.split(",")[0].trim();   // 상세 주소는 빼고 도로명 주소만
        try {
            Response body = client.get()
                    .uri(URL + "?query={q}", query)
                    .header("Authorization", "KakaoAK " + restKey)
                    .retrieve()
                    .body(Response.class);
            if (body == null || body.documents() == null || body.documents().isEmpty()) {
                return Optional.empty();
            }
            Doc first = body.documents().get(0);
            return Optional.of(new LatLng(new BigDecimal(first.y()), new BigDecimal(first.x())));
        } catch (RuntimeException e) {
            log.warn("주소 좌표 변환 실패 ({}): {}", query, e.getMessage());
            return Optional.empty();
        }
    }
}
