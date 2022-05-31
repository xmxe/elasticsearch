package com.xmxe.config.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDocument {
    private String id;
    private String name;
    private String sex;
    private Integer age;
    private String city;
    private Date createTime;
}
