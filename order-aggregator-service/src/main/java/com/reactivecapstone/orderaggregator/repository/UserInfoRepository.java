package com.reactivecapstone.orderaggregator.repository;

import com.reactivecapstone.orderaggregator.model.UserInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserInfoRepository extends ReactiveMongoRepository<UserInfo, String> {
    // This interface gives us reactive methods like findById() for free.
}
