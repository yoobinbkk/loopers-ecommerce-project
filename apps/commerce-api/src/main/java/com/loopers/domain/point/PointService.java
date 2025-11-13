package com.loopers.domain.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointService {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public Optional<Point> findByUserLoginId(String loginId) {
        return pointRepository.findByUser_loginId(loginId);
    }

    @Transactional
    public Optional<Point> savePoint(Point point) {
        return pointRepository.save(point);
    }
}
