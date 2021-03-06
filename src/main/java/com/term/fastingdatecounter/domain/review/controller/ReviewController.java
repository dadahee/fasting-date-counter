package com.term.fastingdatecounter.domain.review.controller;

import com.term.fastingdatecounter.domain.food.domain.Food;
import com.term.fastingdatecounter.domain.food.dto.FoodResponse;
import com.term.fastingdatecounter.domain.food.service.FoodService;
import com.term.fastingdatecounter.domain.review.dto.ReviewResponse;
import com.term.fastingdatecounter.domain.review.domain.Review;
import com.term.fastingdatecounter.domain.review.service.ReviewService;
import com.term.fastingdatecounter.domain.user.dto.SessionUser;
import com.term.fastingdatecounter.domain.user.domain.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "리뷰 관련 페이지")
@RequiredArgsConstructor
@RequestMapping("/food/{foodId}/reviews")
@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final FoodService foodService;

    @Operation(summary = "리뷰 목록 페이지")
    @GetMapping
    public String review(
            Model model,
            @PathVariable(name = "foodId") Long foodId,
            @LoginUser SessionUser user
    ){
        List<Review> reviewList = reviewService.findByFoodId(user.getId(), foodId); // find review list by session user id
        List<ReviewResponse> reviewListResponse = reviewList.stream()
                .map(ReviewResponse::new)
                .collect(Collectors.toList());
        Food food = foodService.findById(foodId);

        model.addAttribute("food", new FoodResponse(food));
        model.addAttribute("user", user);
        model.addAttribute("reviewList", reviewListResponse);
        return "review";
    }

    @Operation(summary = "리뷰 작성 페이지")
    @GetMapping("/save")
    public String reviewSaveForm(
            Model model,
            @PathVariable(name = "foodId") Long foodId
    ) {
        Food food = foodService.findFoodById(foodId);
        model.addAttribute("food", new FoodResponse(food));
        return "review-save";
    }

    @Operation(summary = "리뷰 수정 페이지")
    @GetMapping("/update/{reviewId}")
    public String reviewUpdateForm(
            Model model,
            @PathVariable(name = "foodId") Long foodId,
            @PathVariable(name = "reviewId") Long reviewId
    ){
        Food food = foodService.findFoodById(foodId);
        Review review = reviewService.findById(reviewId);
        model.addAttribute("food", new FoodResponse(food));
        model.addAttribute("review", new ReviewResponse(review));
        return "review-update";
    }
}
