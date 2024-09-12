package ua.nadiiarubantseva.todo.user.role;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Builder
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq_gen")
    @SequenceGenerator(name = "role_seq_gen", sequenceName = "role_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Name name;

    public enum Name {
        ADMIN,
        USER
    }
}
