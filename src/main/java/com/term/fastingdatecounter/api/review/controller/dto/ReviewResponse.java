package com.term.fastingdatecounter.api.review.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.term.fastingdatecounter.api.review.domain.Review;
import lombok.Getter;

import java.util.Date;

@Getter
public class ReviewResponse {

    private final Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final Date date;
    private final String title;
    private final String content;
    private final boolean fasted;

    public ReviewResponse(Review review) {
        this.id = review.getId();
        this.date = review.getDate();
        this.title = review.getTitle();
        this.content = review.getContent();
        this.fasted = review.isFasted();
    }
}
