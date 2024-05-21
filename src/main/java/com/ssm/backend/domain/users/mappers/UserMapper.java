package com.ssm.backend.domain.users.mappers;

import com.ssm.backend.domain.users.dto.UserMst;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<UserMst> selectOneUserWithId(long userId);
    Optional<UserMst> selectOneUserWithEmail(String userEmail);

    long insertOneUser(UserMst userMst);
}
