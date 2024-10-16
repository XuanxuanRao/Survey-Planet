package org.example.service;

import org.example.dto.ResponseDTO;
import org.example.entity.response.Response;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @ClassName ResponseService
 * @description
 */
public interface ResponseService {

    void submit(ResponseDTO responseDTO);

    List<Response> getResponseBySid(Long sid);

    List<Response> getResponseByUid(Long uid);

    void deleteBySid(Long sid);

}
