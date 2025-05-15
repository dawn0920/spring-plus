package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    // 현재 트랜잭션이 있더라도 무시하고, 새로운 트랜잭션을 생성해 메서드를 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(String message) {
        Log log = new Log();
        log.setMessage(message);
        logRepository.save(log);
    }

}
