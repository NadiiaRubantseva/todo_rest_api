package ua.nadiiarubantseva.todo.attachment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Table(name = "attachments")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_seq_gen")
    @SequenceGenerator(name = "attachment_seq_gen", sequenceName = "attachment_seq", allocationSize = 1)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileLink;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
