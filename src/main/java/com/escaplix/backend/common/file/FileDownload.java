package com.escaplix.backend.common.file;

import org.springframework.core.io.Resource;

/** 관리자 서류 이미지 응답용 (A-11, C-15). 컨트롤러가 Content-Type 을 붙여 그대로 내려 준다 */
public record FileDownload(Resource resource, String contentType) {
}
