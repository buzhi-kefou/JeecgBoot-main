package org.jeecg.modules.erp.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.erp.service.IErpSipmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/erp/sipm")
public class ErpSipmController {

    @Resource
    private IErpSipmService erpSipmService;

    @GetMapping("/auth")
    public Result<String> auth() {
        return Result.ok(erpSipmService.auth());
    }

    @GetMapping("/list")
    public Result<Object> list(@RequestParam("no") String no,
                               @RequestParam(value = "start", required = false) Integer start,
                               @RequestParam(value = "size", required = false) Integer size) {
        return Result.ok(erpSipmService.searchPartList(no, start, size));
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> image(@RequestParam("id") String id, @RequestParam(value = "type", required = false) String type) {
        return erpSipmService.getImageUrl(id, type);
    }
}
