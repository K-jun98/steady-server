package dev.steady.review.dto.response;

import dev.steady.review.domain.Card;

public record CardResponse(
        Long cardId,
        String content
) {

    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getContent()
        );
    }

}
