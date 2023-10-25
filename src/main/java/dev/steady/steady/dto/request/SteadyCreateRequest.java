package dev.steady.steady.dto.request;

import dev.steady.steady.domain.Promotion;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyMode;
import dev.steady.steady.domain.SteadyType;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SteadyCreateRequest(
        String name,
        SteadyType type,
        int recruitCount,
        SteadyMode steadyMode,
        LocalDate openingDate,
        LocalDate deadline,
        String title,
        String content,
        List<String> questionList
) {

    public Steady toEntity(Promotion promotion) {
        return Steady.builder()
                .name(name)
                .type(type)
                .recruitCount(recruitCount)
                .steadyMode(steadyMode)
                .openingDate(openingDate)
                .deadline(deadline)
                .title(title)
                .content(content)
                .promotion(promotion)
                .build();
    }

}
