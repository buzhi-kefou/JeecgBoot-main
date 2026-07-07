package org.jeecg.modules.erp.service;

import org.springframework.http.ResponseEntity;

public interface IErpSipmService {

    String auth();

    Object searchPartList(String no, Integer start, Integer size);

    ResponseEntity<byte[]> getImageUrl(String id, String type);
}
