package com.classified.entity;

import com.classified.dto.user.UserStatisticsDto;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SqlResultSetMapping;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;

@Entity
@Subselect("SELECT 1")
@Immutable
@SqlResultSetMapping(
        name = "UserStatisticsMapping",
        classes = @ConstructorResult(
                targetClass = UserStatisticsDto.class,
                columns = {
                        @ColumnResult(name = "user_id", type = Long.class),
                        @ColumnResult(name = "user_name", type = String.class),
                        @ColumnResult(name = "rating", type = BigDecimal.class),
                        @ColumnResult(name = "total_ads", type = Long.class),
                        @ColumnResult(name = "total_sales", type = Long.class),
                        @ColumnResult(name = "total_revenue", type = BigDecimal.class)
                }
        )
)
public class ResultSetMappings {
    @Id
    private Long id;
}