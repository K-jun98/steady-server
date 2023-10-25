package dev.steady.steady.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "forms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SteadyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "order", nullable = false)
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    private Steady steady;

    @Builder
    public SteadyQuestion(String content, int order, Steady steady) {
        this.content = content;
        this.order = order;
        this.steady = steady;
    }

}
