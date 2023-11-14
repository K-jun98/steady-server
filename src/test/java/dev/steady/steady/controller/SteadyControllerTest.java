package dev.steady.steady.controller;

import com.epages.restdocs.apispec.Schema;
import dev.steady.global.auth.Authentication;
import dev.steady.global.config.ControllerTestConfig;
import dev.steady.steady.dto.SearchConditionDto;
import dev.steady.steady.dto.request.SteadyPageRequest;
import dev.steady.steady.dto.request.SteadyQuestionUpdateRequest;
import dev.steady.steady.dto.request.SteadySearchRequest;
import dev.steady.steady.dto.response.ParticipantsResponse;
import dev.steady.steady.dto.response.SteadyDetailResponse;
import dev.steady.steady.dto.response.SteadyQuestionsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.steady.fixture.SteadyFixtures.*;
import static dev.steady.user.fixture.UserFixtures.createPosition;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SteadyControllerTest extends ControllerTestConfig {

    @Test
    @DisplayName("새로운 스테디를 생성하고 생성된 스테디 조회 URL 을 반환한다.")
    void createSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var stackId = 1L;
        var positionId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);
        var steadyRequest = createSteadyRequest(stackId, positionId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        given(steadyService.create(steadyRequest, userInfo)).willReturn(steadyId);

        // when & then
        mockMvc.perform(post("/api/v1/steadies")
                        .header(AUTHORIZATION, TOKEN)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(steadyRequest)))
                .andDo(document("steady-create",
                        resourceDetails().tag("스테디").description("스테디 생성 요청")
                                .requestSchema(Schema.schema("SteadyCreateRequest")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("name").type(STRING).description("스테디 이름"),
                                fieldWithPath("bio").type(STRING).description("스테디 소개"),
                                fieldWithPath("type").type(STRING).description("스테디 종류"),
                                fieldWithPath("participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("steadyMode").type(STRING).description("스테디 진행 방식"),
                                fieldWithPath("scheduledPeriod").type(STRING).description("스테디 시작 예정일"),
                                fieldWithPath("deadline").type(STRING).description("모집 마감일"),
                                fieldWithPath("title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content").type(STRING).description("모집글 내용"),
                                fieldWithPath("positions").type(ARRAY).description("스테디 모집 분야"),
                                fieldWithPath("stacks").type(ARRAY).description("스테디 기술 스택"),
                                fieldWithPath("questions").type(ARRAY).description("스테디 질문 리스트")
                        )
                ))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl(String.format("/api/v1/steadies/%d", steadyId)));
    }

    @Test
    @DisplayName("전체 스테디를 반환한다.")
    void getSteadiesTest() throws Exception {
        // given
        var request = new SteadyPageRequest(0, "desc");
        MultiValueMap params = new LinkedMultiValueMap<>() {{
            add("page", "0");
            add("direction", "desc");
        }};

        var pageable = request.toPageable();
        var steady = createSteady();
        var response = createSteadyPageResponse(steady, pageable);

        given(steadyService.getSteadies(pageable)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/steadies")
                        .queryParams(params))
                .andDo(document("steady-get-steadies",
                        resourceDetails().tag("스테디").description("스테디 전체 조회")
                                .responseSchema(Schema.schema("PageResponse")),
                        queryParameters(
                                parameterWithName("page").description("요청 페이지 번호"),
                                parameterWithName("direction").description("내림/오름차순")
                        ),
                        responseFields(
                                fieldWithPath("content[].id").type(NUMBER).description("스테디 id"),
                                fieldWithPath("content[].nickname").type(STRING).description("스테디 리더 닉네임"),
                                fieldWithPath("content[].profileImage").type(STRING).description("스테디 리더 프로필 이미지"),
                                fieldWithPath("content[].title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content[].type").type(STRING).description("스테디 분류"),
                                fieldWithPath("content[].status").type(STRING).description("스테디 상태"),
                                fieldWithPath("content[].deadline").type(STRING).description("모집 마감일"),
                                fieldWithPath("content[].createdAt").type(STRING).description("스테디 생성일"),
                                fieldWithPath("content[].participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("content[].numberOfParticipants").type(NUMBER).description("스테디 참여 인원"),
                                fieldWithPath("content[].stacks[].id").type(NUMBER).description("기술 스택 id"),
                                fieldWithPath("content[].stacks[].name").type(STRING).description("기술 스택명"),
                                fieldWithPath("content[].stacks[].imageUrl").type(STRING).description("기술 스택 이미지"),
                                fieldWithPath("numberOfElements").type(NUMBER).description("현재 페이지 조회된 개수"),
                                fieldWithPath("page").type(NUMBER).description("현재 페이지"),
                                fieldWithPath("size").type(NUMBER).description("페이지 크기"),
                                fieldWithPath("totalPages").type(NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("totalElements").type(NUMBER).description("전체 개수")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("검색 조건에 따른 전체 조회 결과를 반환한다.")
    void getSteadiesByConditionTest() throws Exception {
        // given
        var searchRequest = new SteadySearchRequest(null,
                0,
                "desc",
                null,
                "online",
                "Java",
                "Backend",
                "recruiting",
                "false",
                "");
        MultiValueMap params = new LinkedMultiValueMap<>() {{
            add("steadyType", null);
            add("page", "0");
            add("direction", "desc");
            add("criteria", null);
            add("steadyMode", "online");
            add("stack", "Java");
            add("position", "Backend");
            add("status", "recruiting");
            add("like", "false");
            add("keyword", "");
        }};

        var pageable = searchRequest.toPageable();
        var condition = SearchConditionDto.from(searchRequest);
        var steady = createSteady();
        var response = createSteadyPageResponse(steady, pageable);

        given(steadyService.getSteadies(condition, pageable)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/steadies/search")
                        .queryParams(params))
                .andDo(document("steady-get-steadies-by-condition",
                        resourceDetails().tag("스테디").description("스테디 검색 및 필터링 조회")
                                .responseSchema(Schema.schema("PageResponse")),
                        queryParameters(
                                parameterWithName("steadyType").description("스테디 타입").optional(),
                                parameterWithName("page").description("요청 페이지 번호"),
                                parameterWithName("direction").description("내림/오름차순").optional(),
                                parameterWithName("criteria").description("정렬 조건").optional(),
                                parameterWithName("steadyMode").description("스테디 진행 방식").optional(),
                                parameterWithName("stack").description("스테디 기술 스택").optional(),
                                parameterWithName("position").description("스테디 포지션").optional(),
                                parameterWithName("status").description("스테디 상태").optional(),
                                parameterWithName("like").description("내 좋아요"),
                                parameterWithName("keyword").description("검색 키워드").optional()
                        ),
                        responseFields(
                                fieldWithPath("content[].id").type(NUMBER).description("스테디 id"),
                                fieldWithPath("content[].nickname").type(STRING).description("스테디 리더 닉네임"),
                                fieldWithPath("content[].profileImage").type(STRING).description("스테디 리더 프로필 이미지"),
                                fieldWithPath("content[].title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content[].type").type(STRING).description("스테디 분류"),
                                fieldWithPath("content[].status").type(STRING).description("스테디 상태"),
                                fieldWithPath("content[].deadline").type(STRING).description("모집 마감일"),
                                fieldWithPath("content[].createdAt").type(STRING).description("스테디 생성일"),
                                fieldWithPath("content[].participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("content[].numberOfParticipants").type(NUMBER).description("스테디 참여 인원"),
                                fieldWithPath("content[].stacks[].id").type(NUMBER).description("기술 스택 id"),
                                fieldWithPath("content[].stacks[].name").type(STRING).description("기술 스택명"),
                                fieldWithPath("content[].stacks[].imageUrl").type(STRING).description("기술 스택 이미지"),
                                fieldWithPath("numberOfElements").type(NUMBER).description("현재 페이지 조회된 개수"),
                                fieldWithPath("page").type(NUMBER).description("현재 페이지"),
                                fieldWithPath("size").type(NUMBER).description("페이지 크기"),
                                fieldWithPath("totalPages").type(NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("totalElements").type(NUMBER).description("전체 개수")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("스테디 Id를 통해 스테디를 상세 조회할 수 있다.")
    void getDetailSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        var steady = createSteady();
        var position = createPosition();
        var steadyPosition = createSteadyPosition(steady, position);
        ReflectionTestUtils.setField(steadyPosition, "id", 1L);
        var response = SteadyDetailResponse.of(steady,
                List.of(steadyPosition),
                true,
                false);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        given(steadyService.getDetailSteady(steadyId, userInfo)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/steadies/{steadyId}", steadyId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-get-detail-steady",
                        resourceDetails().tag("스테디").description("스테디 상세조회")
                                .responseSchema(Schema.schema("SteadyDetailResponse")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰").optional()
                        ),
                        responseFields(
                                fieldWithPath("id").type(NUMBER).description("스테디 id"),
                                fieldWithPath("leaderResponse.id").type(NUMBER).description("스테디 리더 id"),
                                fieldWithPath("leaderResponse.nickname").type(STRING).description("스테디 리더 닉네임"),
                                fieldWithPath("leaderResponse.profileImage").type(STRING).description("스테디 리더 프로필 이미지"),
                                fieldWithPath("name").type(STRING).description("스테디 이름"),
                                fieldWithPath("bio").type(STRING).description("스테디 소개"),
                                fieldWithPath("type").type(STRING).description("스테디 유형"),
                                fieldWithPath("status").type(STRING).description("스테디 상태"),
                                fieldWithPath("participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("numberOfParticipants").type(NUMBER).description("스테디 참여 인원"),
                                fieldWithPath("steadyMode").type(STRING).description("스테디 진행 방식"),
                                fieldWithPath("scheduledPeriod").type(STRING).description("예상 기간"),
                                fieldWithPath("deadline").type(STRING).description("마감일"),
                                fieldWithPath("title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content").type(STRING).description("모집글 내용"),
                                fieldWithPath("positions[].id").type(NUMBER).description("포지션 id"),
                                fieldWithPath("positions[].name").type(STRING).description("포지션 이름"),
                                fieldWithPath("stacks[].id").type(NUMBER).description("기술 스택 id"),
                                fieldWithPath("stacks[].name").type(STRING).description("기술 스택명"),
                                fieldWithPath("stacks[].imageUrl").type(STRING).description("기술 스택 이미지"),
                                fieldWithPath("isLeader").type(BOOLEAN).description("리더 여부"),
                                fieldWithPath("isSubmittedUser").type(BOOLEAN).description("신청 여부"),
                                fieldWithPath("promotionCount").type(NUMBER).description("끌어올리가 남은 횟수"),
                                fieldWithPath("createdAt").type(STRING).description("스테디 생성일"),
                                fieldWithPath("finishedAt").type(STRING).description("스테디 종료일").optional(),
                                fieldWithPath("isReviewEnabled").type(BOOLEAN).description("리뷰 작성 가능 여부")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 질문을 조회할 수 있다.")
    void getSteadyQuestionsTest() throws Exception {
        var steadyId = 1L;
        SteadyQuestionsResponse steadyQuestionsResponse = createSteadyQuestionsResponse();

        given(steadyService.getSteadyQuestions(steadyId)).willReturn(steadyQuestionsResponse);

        mockMvc.perform(get("/api/v1/steadies/{steadyId}/steadyQuestions", steadyId))
                .andDo(document("steady-get-steadyQuestions",
                        resourceDetails().tag("스테디").description("스테디 질문 조회")
                                .responseSchema(Schema.schema("SteadyQuestionsResponse")),
                        responseFields(
                                fieldWithPath("steadyQuestions[].id").type(NUMBER).description("스테디 질문 식별자"),
                                fieldWithPath("steadyQuestions[].content").type(STRING).description("질문 내용"),
                                fieldWithPath("steadyQuestions[].sequence").type(NUMBER).description("질문 순서")
                        )
                )).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(steadyQuestionsResponse)));
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 참여자 목록을 조회할 수 있다.")
    void getSteadyParticipantsTest() throws Exception {
        var steadyId = 1L;
        ParticipantsResponse participantsResponse = createParticipantsResponse();

        given(steadyService.getSteadyParticipants(steadyId)).willReturn(participantsResponse);

        mockMvc.perform(get("/api/v1/steadies/{steadyId}/participants", steadyId))
                .andDo(document("steady-get-participants",
                        resourceDetails().tag("스테디").description("스테디 참여자 조회")
                                .responseSchema(Schema.schema("ParticipantsResponse")),
                        responseFields(
                                fieldWithPath("participants[].id").type(NUMBER).description("참여자 식별자"),
                                fieldWithPath("participants[].nickname").type(STRING).description("참여자 닉네임"),
                                fieldWithPath("participants[].profileImage").type(STRING).description("참여자 프로필 이미지"),
                                fieldWithPath("participants[].isLeader").type(BOOLEAN).description("리더 여부")
                        )
                )).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(participantsResponse)));
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 정보를 수정할 수 있다.")
    void updateSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var stackId = 1L;
        var positionId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);
        var request = createSteadyUpdateRequest(stackId, positionId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).updateSteady(steadyId, request, userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}", steadyId)
                        .header(AUTHORIZATION, TOKEN)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(document("steady-update",
                        resourceDetails().tag("스테디").description("스테디 수정")
                                .requestSchema(Schema.schema("SteadyUpdateRequest")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("name").type(STRING).description("스테디 이름"),
                                fieldWithPath("bio").type(STRING).description("스테디 소개"),
                                fieldWithPath("type").type(STRING).description("스테디 종류"),
                                fieldWithPath("status").type(STRING).description("스테디 상태"),
                                fieldWithPath("participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("steadyMode").type(STRING).description("스테디 진행 방식"),
                                fieldWithPath("scheduledPeriod").type(STRING).description("스테디 시작 예정일"),
                                fieldWithPath("deadline").type(STRING).description("모집 마감일"),
                                fieldWithPath("title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content").type(STRING).description("모집글 내용"),
                                fieldWithPath("positions").type(ARRAY).description("스테디 모집 분야"),
                                fieldWithPath("stacks").type(ARRAY).description("스테디 기술 스택")
                        )
                ))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 질문을 수정할 수 있다.")
    void updateSteadyQuestionsTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var userInfo = createUserInfo(userId);
        var authentication = new Authentication(userId);
        var steadyQuestionUpdateRequest = new SteadyQuestionUpdateRequest(List.of("변경된 질문1", "변경된 질문2"));

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).updateSteadyQuestions(steadyId, steadyQuestionUpdateRequest, userInfo);

        //when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}/steadyQuestions", steadyId)
                        .header(AUTHORIZATION, TOKEN)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(steadyQuestionUpdateRequest)))
                .andDo(document("steady-update-steady-questions",
                        resourceDetails().tag("스테디").description("스테디 질문 수정")
                                .requestSchema(Schema.schema("SteadyQuestionUpdateRequest")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("questions").type(ARRAY).description("질문 목록")
                        )
                )).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 리더의 끌어올리기 요청을 통해 스테디를 끌어올릴 수 있다.")
    void promoteSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).promoteSteady(steadyId, userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}/promote", steadyId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-promote",
                        resourceDetails().tag("스테디").description("스테디 끌어올리기"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 리더의 종료 요청을 통해 스테디를 끌어올릴 수 있다.")
    void finishSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).promoteSteady(steadyId, userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}/finish", steadyId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-finish",
                        resourceDetails().tag("스테디").description("스테디 종료"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 삭제 요청을 통해 스테디를 삭제할 수 있다.")
    void deleteSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).promoteSteady(steadyId, userInfo);

        // when & then
        mockMvc.perform(delete("/api/v1/steadies/{steadyId}", steadyId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-delete",
                        resourceDetails().tag("스테디").description("스테디 삭제"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )))
                .andExpect(status().isNoContent());
    }

}
