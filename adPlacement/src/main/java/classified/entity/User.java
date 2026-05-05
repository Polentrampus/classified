package classified.entity;

import classified.dto.user.UserStatisticsDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/// Реализовала для витрины данных, но нарушает принцип разделения на слои
@SqlResultSetMapping(
        name = "UserStatisticsMapping",
        classes = @ConstructorResult(
                targetClass = UserStatisticsDto.class,
                columns = {
                        @ColumnResult(name = "userId", type = Long.class),
                        @ColumnResult(name = "userName", type = String.class),
                        @ColumnResult(name = "rating", type = BigDecimal.class),
                        @ColumnResult(name = "totalAds", type = Long.class),
                        @ColumnResult(name = "totalSales", type = Long.class),
                        @ColumnResult(name = "totalRevenue", type = BigDecimal.class)
                }
        )
)
@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Email
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Size(max = 20)
    @Column(unique = true, length = 20)
    private String phone;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String password;

    /// TODO: как то исправить то, что в отношении 1:1 игнорируется ленивая зависимость и тянется все из БД
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UserRating userRating;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}