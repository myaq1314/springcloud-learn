package com.zz.scservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class UserInfo {
    private String userName;
    private String userId;
    private Date lastedTime;
}
