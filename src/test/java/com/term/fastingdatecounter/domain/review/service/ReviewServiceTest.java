package com.term.fastingdatecounter.domain.review.service;

import com.term.fastingdatecounter.domain.food.domain.Food;
import com.term.fastingdatecounter.domain.food.repository.FoodRepository;
import com.term.fastingdatecounter.domain.food.service.FoodService;
import com.term.fastingdatecounter.domain.review.domain.Review;
import com.term.fastingdatecounter.domain.review.dto.ReviewRequest;
import com.term.fastingdatecounter.domain.review.repository.ReviewRepository;
import com.term.fastingdatecounter.domain.user.domain.User;
import com.term.fastingdatecounter.domain.user.repository.UserRepository;
import com.term.fastingdatecounter.global.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@AutoConfigureMockMvc
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private FoodService foodService;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private User user;
    private Food food;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(123L)
                .name("test")
                .email("test@test.com")
                .build();
        food = Food.builder()
                .id(123L)
                .user(user)
                .name("test-food")
                .startDate(LocalDate.of(2021, 12, 1))
                .build();
    }

    @AfterEach
    void cleanAll() {
        foodRepository.deleteAll();
        userRepository.deleteAll();
        reviewRepository.deleteAll();
    }

    private Review createReview(Long id, LocalDate date) {
        return Review.builder()
                .id(id)
                .food(food)
                .date(date)
                .title("review title")
                .content("review content")
                .fasted(true)
                .build();
    }

    private ReviewRequest createReviewRequest(LocalDate date) {
        return ReviewRequest.builder()
                .date(date)
                .title("review title")
                .content("review content")
                .fasted(true)
                .build();
    }

    private ReviewRequest createReviewRequest(String title, String content, LocalDate date, boolean fasted) {
        return ReviewRequest.builder()
                .date(date)
                .title(title)
                .content(content)
                .fasted(fasted)
                .build();
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ??????")
    void findByFoodId() {
        // given
        //// ?????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ?????? ??????
        given(foodRepository.findById(food.getId())).willReturn(Optional.of(food));

        //// ????????? ?????????
        Review review1 = createReview(1L, LocalDate.now().minusDays(3));
        Review review2 = createReview(2L, LocalDate.now().minusDays(2));
        Review review3 = createReview(3L, LocalDate.now().minusDays(1));
        List<Review> reviews = Arrays.asList(review3, review2, review1);


        //// ?????? ?????? ????????? ??????
        given(reviewRepository.findByFoodIdOrderByDateDesc(food.getId())).willReturn(reviews);

        // when
        List<Review> findResult = reviewService.findByFoodId(user.getId(), food.getId());

        // then
        assertThat(findResult).hasSize(3);
        assertThat(findResult).containsSequence(Arrays.asList(review3, review2, review1));
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ??????(???????????? ?????? ??????)")
    void findByFoodIdFailedWhenNotExistUser() {
        // given
        //// ?????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> reviewService.findByFoodId(user.getId(), food.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("G04");
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ??????(???????????? ?????? ??????)")
    void findByFoodIdFailedWhenNotExistFood() {
        // given
        //// ?????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ?????? ??????
        given(foodRepository.findById(food.getId())).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> reviewService.findByFoodId(user.getId(), food.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("F03");
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ??????(?????? ???????????? ?????? ??????)")
    void findByFoodIdFailedWhenUserWithoutAuthority() {
        // given
        //// ?????? ??????
        User anotherUser = User.builder()
                .id(3333L)
                .name("foreigner")
                .email("foreigner@test.com")
                .build();

        //// ?????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userRepository.findById(anotherUser.getId())).willReturn(Optional.of(anotherUser));

        //// ?????? ??????
        given(foodRepository.findById(food.getId())).willReturn(Optional.of(food));

        // when
        // then
        assertThatThrownBy(() -> reviewService.findByFoodId(anotherUser.getId(), food.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("G02");
    }


    @Test
    @DisplayName("?????? ?????? - ??????")
    void save() {
        // given
        //// ???????????? ?????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ?????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ????????? ?????? ?????????
        ReviewRequest request = createReviewRequest(LocalDate.now());

        // when
        Review result = reviewService.save(user.getId(), food.getId(), request);

        // then
        assertAll(
                () -> assertThat(result.getFood().getId()).isEqualTo(food.getId()),
                () -> assertThat(result.getDate()).isEqualTo(request.getDate()),
                () -> assertThat(result.getTitle()).isEqualTo(request.getTitle()),
                () -> assertThat(result.getContent()).isEqualTo(request.getContent()),
                () -> assertThat(result.isFasted()).isEqualTo(request.isFasted())
        );
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ??????)")
    void saveFailedWhenNotExistUser() {
        // given
        //// ???????????? ?????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        //// ???????????? ?????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ????????? ?????? ?????????
        ReviewRequest request = createReviewRequest(LocalDate.now());

        // when
        // then
        assertThatThrownBy(() -> reviewService.save(user.getId(), food.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("G04");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ??????)")
    void saveFailedWhenNotExistFood() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ?????? ?????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.empty());

        //// ????????? ?????? ?????????
        ReviewRequest request = createReviewRequest(LocalDate.now());

        // when
        // then
        assertThatThrownBy(() -> reviewService.save(user.getId(), food.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("F03");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(?????? ???????????? ?????? ????????? ??????)")
    void saveFailedWhenUserWithoutAuthority() {
        // given
        //// ?????? ??????
        User anotherUser = User.builder()
                .id(3333L)
                .name("foreigner")
                .email("foreigner@test.com")
                .build();

        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userRepository.findById(anotherUser.getId())).willReturn(Optional.of(anotherUser));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ????????? ?????? ?????????
        ReviewRequest request = createReviewRequest(LocalDate.now());

        // when
        // then
        assertThatThrownBy(() -> reviewService.save(anotherUser.getId(), food.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("G02");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(?????? ??????)")
    void saveFailedWhenDateConflicts() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        LocalDate today = LocalDate.now();

        //// ????????? ?????? ?????????
        ReviewRequest request = createReviewRequest(LocalDate.now());

        //// ????????? ?????? ????????? ?????? ????????? ??????
        Review writtenReview = createReview(1L, today);
        given(reviewRepository.findByFoodIdAndDate(food.getId(), today)).willReturn(Optional.of(writtenReview));

        // when
        // then
        assertThatThrownBy(() -> reviewService.save(user.getId(), food.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("R08");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(?????? ????????? ????????????????????? ??????)")
    void saveFailedWhenDateIsInvalid() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ????????? ?????? ?????????
        ReviewRequest request = createReviewRequest(LocalDate.of(2020, 1, 1));

        // when
        // then
        assertThatThrownBy(() -> reviewService.save(user.getId(), food.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("R09");
    }

    @Test
    @DisplayName("?????? ?????? - ??????")
    void update() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        //// ????????? ?????????
        ReviewRequest request = createReviewRequest("new title", "new content", LocalDate.now(), false);

        // when
        Review result = reviewService.update(user.getId(), food.getId(), review.getId(), request);

        // then
        assertAll(
                () -> assertThat(review.getId()).isEqualTo(result.getId()),
                () -> assertThat(request.getDate()).isEqualTo(result.getDate()),
                () -> assertThat(request.getTitle()).isEqualTo(result.getTitle()),
                () -> assertThat(request.getContent()).isEqualTo(result.getContent()),
                () -> assertThat(request.isFasted()).isEqualTo(result.isFasted())
        );
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ??????)")
    void updateFailedWhenNotExistUser() {
        // given
        //// ???????????? ?????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        //// ????????? ?????????
        ReviewRequest request = createReviewRequest("new title", "new content", LocalDate.now(), false);

        // when
        // then
        assertThatThrownBy(() -> reviewService.update(user.getId(), food.getId(), review.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("G04");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ??????)")
    void updateFailedWhenNotExistFood() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.empty());

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        //// ????????? ?????????
        ReviewRequest request = createReviewRequest("new title", "new content", LocalDate.now(), false);

        // when
        // then
        assertThatThrownBy(() -> reviewService.update(user.getId(), food.getId(), review.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("F03");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ??????)")
    void updateFailedWhenNotExistReview() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.empty());

        //// ????????? ?????????
        ReviewRequest request = createReviewRequest("new title", "new content", LocalDate.now(), false);

        // when
        // then
        assertThatThrownBy(() -> reviewService.update(user.getId(), food.getId(), review.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("R07");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ????????? ??????)")
    void updateFailedWhenUserWithoutAuthority() {
        // given
        //// ?????? ??????
        User anotherUser = User.builder()
                .id(444L)
                .name("foreigner")
                .email("foreigner@test.com")
                .build();

        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userRepository.findById(anotherUser.getId())).willReturn(Optional.of(anotherUser));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        //// ????????? ?????????
        ReviewRequest request = createReviewRequest("new title", "new content", LocalDate.now(), false);

        // when
        // then
        assertThatThrownBy(() -> reviewService.update(anotherUser.getId(), food.getId(), review.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("G02");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(?????? ??????)")
    void updateFailedWhenDateConflicts() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        //// ????????? ?????????
        LocalDate newDate = LocalDate.of(2021, 12, 3);
        ReviewRequest request = createReviewRequest("new title", "new content", newDate, false);

        //// ?????? ????????? ?????? ????????? ??????
        Review writtenReview = createReview(11L, newDate);
        given(reviewRepository.findByFoodIdAndDate(food.getId(), newDate)).willReturn(Optional.of(writtenReview));

        // when
        // then
        assertThatThrownBy(() -> reviewService.update(user.getId(), food.getId(), review.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("R08");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(?????? ????????? ????????????????????? ??????)")
    void updateFailedWhenDateIsInvalid() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        //// ????????? ?????????
        ReviewRequest request = createReviewRequest("new title", "new content", LocalDate.MIN, false);

        // when
        // then
        assertThatThrownBy(() -> reviewService.update(user.getId(), food.getId(), review.getId(), request))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("R09");
    }

    @Test
    @DisplayName("?????? ?????? - ??????")
    void delete() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        // when
        reviewService.delete(user.getId(), food.getId(), review.getId());

        // then
        then(reviewRepository)
                .should(times(1))
                .findById(review.getId());
        then(reviewRepository)
                .should(times(1))
                .delete(any(Review.class));
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ??????)")
    void deleteFailedWhenNotExistUser() {
        // given
        //// ???????????? ?????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        // when
        // then
        assertThatThrownBy(() -> reviewService.delete(user.getId(), food.getId(), review.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("G04");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ??????)")
    void deleteFailedWhenNotExistFood() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.empty());

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        // when
        // then
        assertThatThrownBy(() -> reviewService.delete(user.getId(), food.getId(), review.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("F03");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ??????)")
    void deleteFailedWhenNotExistReview() {
        // given
        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> reviewService.delete(user.getId(), food.getId(), review.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("R07");
    }

    @Test
    @DisplayName("?????? ?????? - ??????(???????????? ?????? ????????? ??????)")
    void deleteFailedWhenUserWithoutAuthority() {
        // given
        //// ?????? ??????
        User anotherUser = User.builder()
                .id(3333L)
                .name("foreigner")
                .email("foreigner@test.com")
                .build();

        //// ???????????? ??????
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userRepository.findById(anotherUser.getId())).willReturn(Optional.of(anotherUser));

        //// ???????????? ??????
        given(foodRepository.findById(user.getId())).willReturn(Optional.of(food));

        //// ???????????? ??????
        Review review = createReview(10L, LocalDate.now());
        given(reviewRepository.findById(review.getId())).willReturn(Optional.of(review));

        // when
        // then
        assertThatThrownBy(() -> reviewService.delete(anotherUser.getId(), food.getId(), review.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo("G02");
    }
}