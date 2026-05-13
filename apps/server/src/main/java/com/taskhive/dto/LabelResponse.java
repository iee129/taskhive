package com.taskhive.dto;

import com.taskhive.model.Label;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class LabelResponse {
    private Long id;
    private String name;
    private String color;

    public static LabelResponse from(Label label) {
        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }
}
