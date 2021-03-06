package com.term.fastingdatecounter.domain.review.repository;

import com.term.fastingdatecounter.domain.food.domain.Food;
import com.term.fastingdatecounter.domain.food.repository.FoodRepository;
import com.term.fastingdatecounter.domain.review.domain.Review;
import com.term.fastingdatecounter.domain.user.domain.User;
import com.term.fastingdatecounter.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@ExtendWith(SpringExtension.class)
@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FoodRepository foodRepository;

    private User user;
    private Food food;

    @AfterEach
    public void cleanAll() {
        reviewRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .name("test")
                .email("test@test.com")
                .build();
        food = Food.builder()
                .user(user)
                .name("food")
                .startDate(LocalDate.of(2021, 12, 1))
                .build();
        userRepository.save(user);
        foodRepository.save(food);
    }

    private Review createReview() {
        return Review.builder()
                .food(food)
                .title("review title")
                .content("review content")
                .date(LocalDate.now())
                .fasted(true)
                .build();
    }

    private Review createReview(LocalDate date, boolean fasted) {
        return Review.builder()
                .food(food)
                .title("review title")
                .content("review content")
                .date(date)
                .fasted(fasted)
                .build();
    }

    @Test
    @DisplayName("?????? ??????")
    void save() {
        // given
        Review review = createReview();
        LocalDateTime now = LocalDateTime.now();

        // when
        reviewRepository.save(review);

        // then
        //// ?????? ??????
        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).isNotEmpty();

        Review result = reviews.get(0);
        //// ?????? ?????? ??????
        assertAll(
                () -> assertThat(result.getFood().getId()).isEqualTo(review.getFood().getId()),
                () -> assertThat(result.getTitle()).isEqualTo(review.getTitle()),
                () -> assertThat(result.getContent()).isEqualTo(review.getContent()),
                () -> assertThat(result.getDate()).isEqualTo(review.getDate()),
                () -> assertThat(result.isFasted()).isEqualTo(review.isFasted())
        );
        //// ???????????? ??????
        assertThat(result.getCreatedAt()).isAfterOrEqualTo(now);
    }

    @Test
    @DisplayName("?????? ??????")
    void update() {
        // given
        //// ?????? ????????? ?????? ????????? ?????? ??????
        Review review = createReview();
        Review saveResult = reviewRepository.save(review);
        LocalDateTime beforeUpdateDateTime = saveResult.getUpdatedAt();

        //// ????????? ?????????
        LocalDate newStartDate = LocalDate.now();
        String newTitle = "review title updated";
        String newContent = "review contnet updated";
        boolean newFasted = false;

        // when
        saveResult.updateReview(newStartDate, newTitle, newContent, newFasted);

        // then
        //// ?????? ?????? ??????
        assertThat(reviewRepository.findById(saveResult.getId())).isPresent();
        Review updateResult = reviewRepository.findById(saveResult.getId()).get();

        //// ?????? ?????? ??????
        assertThat(updateResult.getId()).isEqualTo(saveResult.getId()); // pk ?????? ?????? ?????? ??????
        assertThat(updateResult.getFood().getId()).isEqualTo(saveResult.getFood().getId()); // ?????? id ?????? ??????

        //// ?????? ??????
        assertAll(
                () -> assertThat(updateResult.getDate()).isEqualTo(newStartDate),
                () -> assertThat(updateResult.getTitle()).isEqualTo(newTitle),
                () -> assertThat(updateResult.getContent()).isEqualTo(newContent),
                () -> assertThat(updateResult.isFasted()).isEqualTo(newFasted)
        );

        //// ???????????? ??????
        assertThat(updateResult.getUpdatedAt()).isAfterOrEqualTo(beforeUpdateDateTime);
    }

    @Test
    @DisplayName("?????? ???????????? ?????? ?????? ?????? (?????? ????????????)")
    void findByFoodIdOrderByDateDesc() {
        // given
        //// ????????? ?????? ??????
        for (int i=1; i<6; i++){
            Review review = createReview(LocalDate.of(2021, 12, i), true);
            reviewRepository.save(review);
        }

        // when
        List<Review> reviews = reviewRepository.findByFoodIdOrderByDateDesc(food.getId());

        // then
        //// ?????? ?????? ??????
        assertThat(reviews).hasSize(5);

        //// ?????? ?????? ??? ?????? ???????????? ??????
        for (int i=0; i<4; i++) {
            // ??????????????? ?????? ???????????? ?????? ?????? ??????
            assertThat(reviews.get(i).getDate()).isEqualTo(LocalDate.of(2021, 12, 5 - i));

            // ?????? ???????????? ?????? ???????????? ??????
            assertThat(reviews.get(i).getDate()).isAfter(reviews.get(i + 1).getDate());

        }
    }

    @Test
    @DisplayName("?????? ????????? & ????????? ?????? ?????? ??????")
    void findByFoodIdAndDate() {
        // given
        //// ????????? ?????? ??????
        Review review = createReview();
        Review saveResult = reviewRepository.save(review);

        // when
        Optional<Review> findResult = reviewRepository.findByFoodIdAndDate(food.getId(), saveResult.getDate());

        // then
        assertThat(findResult).contains(saveResult);
    }

    @Test
    @DisplayName("?????? ?????? ????????? - ?????? ???????????? ?????? ??????(fasted)??? ??????")
    void countByFoodIdAndFastedIsTrue() {
        // given
        for (int i=1; i<6; i++) {
            Review review = createReview(LocalDate.of(2021, 12, i), i % 2 == 0);
            reviewRepository.save(review);
        }

        // when
        Long count = reviewRepository.countByFoodIdAndFastedIsTrue(food.getId());

        // then
        assertThat(count).isEqualTo(2);
    }
}