package com.notification.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NotificationSearchEntity {
    /***
     * SchemeSearchEntity
     */
    HashMap<String,Object> filters;
    List<String> fields;

    String query;

    Integer offset;

    Integer limit;

    String sortField;

    String sortDirection;

    List<String> facets;
}
