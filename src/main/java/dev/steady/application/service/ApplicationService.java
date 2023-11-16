package dev.steady.application.service;

import dev.steady.application.domain.Application;
import dev.steady.application.domain.SurveyResult;
import dev.steady.application.domain.SurveyResults;
import dev.steady.application.domain.repository.ApplicationRepository;
import dev.steady.application.dto.request.ApplicationStatusUpdateRequest;
import dev.steady.application.dto.request.SurveyResultRequest;
import dev.steady.application.dto.response.ApplicationDetailResponse;
import dev.steady.application.dto.response.ApplicationSummaryResponse;
import dev.steady.application.dto.response.CreateApplicationResponse;
import dev.steady.application.dto.response.SliceResponse;
import dev.steady.global.auth.UserInfo;
import dev.steady.notification.domain.ApplicationResultNotificationStrategy;
import dev.steady.notification.domain.FreshApplicationNotificationStrategy;
import dev.steady.notification.domain.NotificationStrategy;
import dev.steady.notification.service.NotificationService;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

import static dev.steady.application.domain.ApplicationStatus.ACCEPTED;
import static dev.steady.application.domain.ApplicationStatus.WAITING;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final UserRepository userRepository;
    private final SteadyRepository steadyRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    @Transactional
    public CreateApplicationResponse createApplication(Long steadyId, List<SurveyResultRequest> request, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);

        Application application = new Application(user, steady);
        createSurveyResult(application, request);

        Application savedApplication = applicationRepository.save(application);
        createNotification(new FreshApplicationNotificationStrategy(steady));
        return CreateApplicationResponse.from(savedApplication);
    }

    @Transactional(readOnly = true)
    public SliceResponse<ApplicationSummaryResponse> getApplications(Long steadyId, UserInfo userInfo, Pageable pageable) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        steady.validateLeader(user);
        Slice<Application> applications = applicationRepository.findAllBySteadyIdAndStatus(steadyId, WAITING, pageable);
        Slice<ApplicationSummaryResponse> responses = applications.map(ApplicationSummaryResponse::from);
        return SliceResponse.from(responses);
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponse getApplicationDetail(Long applicationId, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Application application = applicationRepository.getById(applicationId);
        application.checkAccessOrThrow(user);

        SurveyResults surveyResults = application.getSurveyResults();
        return ApplicationDetailResponse.from(surveyResults);
    }

    @Transactional
    public void updateStatusOfApplication(Long applicationId,
                                          ApplicationStatusUpdateRequest request,
                                          UserInfo userInfo) {
        User leader = userRepository.getUserBy(userInfo.userId());
        Application application = applicationRepository.getById(applicationId);
        application.updateStatus(request.status(), leader);
        if (request.status() == ACCEPTED) {
            addParticipant(application, leader);
        }
        createNotification(new ApplicationResultNotificationStrategy(application));
    }

    private void addParticipant(Application application, User leader) {
        Steady steady = application.getSteady();
        User user = application.getUser();
        steady.addParticipantByLeader(leader, user);
    }

    private List<SurveyResult> createSurveyResult(Application application, List<SurveyResultRequest> surveys) {
        return IntStream.range(0, surveys.size())
                .mapToObj(index -> SurveyResult.create(application,
                        surveys.get(index).question(),
                        surveys.get(index).answer(),
                        index))
                .toList();
    }

    private void createNotification(NotificationStrategy notificationStrategy) {
        try {
            notificationService.create(notificationStrategy);
        } catch (Exception exception) {
            log.warn("알림 생성 오류", exception);
        }
    }

}
