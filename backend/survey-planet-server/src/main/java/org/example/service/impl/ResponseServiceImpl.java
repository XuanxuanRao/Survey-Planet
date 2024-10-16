package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.context.BaseContext;
import org.example.dto.ResponseDTO;
import org.example.entity.response.Response;
import org.example.mapper.ResponseMapper;
import org.example.service.ResponseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @ClassName ResponseServiceImpl
 * @description
 */
@Service
public class ResponseServiceImpl implements ResponseService {

    @Resource
    private ResponseMapper responseMapper;

    @Override
    @Transactional  // 需要两次连接数据库进行插入操作，启用事务
    public void submit(ResponseDTO responseDTO) {
        Long uid = BaseContext.getCurrentId();

        Response response = new Response();
        BeanUtils.copyProperties(responseDTO, response);
        response.setUid(uid);
        response.setCreateTime(LocalDateTime.now());

        responseMapper.insertRecord(response);

        response.getItems().forEach(item -> item.setRid(response.getRid()));
        responseMapper.insertItems(response.getItems());
    }

    @Override
    public List<Response> getResponseBySid(Long sid) {
        return responseMapper.getBySid(sid);
    }

    @Override
    public List<Response> getResponseByUid(Long uid) {
        return null;
    }

    @Override
    public void deleteBySid(Long sid) {
        List<Long> rids = responseMapper.getBySid(sid).stream().map(Response::getRid).toList();
        rids.forEach(rid -> {
            responseMapper.deleteItemsByRid(rid);
            responseMapper.deleteRecordByRid(rid);
        });
    }
}
