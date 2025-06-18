package com.reactivecapstone.orderaggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    @Id
    private String id;
    private String name;
    private String phone;
}