package ua.nadiiarubantseva.todo.task.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import ua.nadiiarubantseva.todo.attachment.Attachment;
import ua.nadiiarubantseva.todo.user.User;

import java.time.LocalDateTime;

@Table(name = "tasks")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq_gen")
    @SequenceGenerator(name = "task_seq_gen", sequenceName = "task_seq", allocationSize = 1)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String description;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;

    @Builder.Default
    @Column(nullable = false)
    private boolean complete = false;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
