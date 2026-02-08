package com.rongproject.JavaSprint5_2LibrarySystem.Entity;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "borrow_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes({
        @CompoundIndex(name = "user_book_idx", def = "{'user_id': 1, 'book_id': 1}")
})
public class BorrowLog {

    @Id
    private String id;

    @Field("user_id")
    private Long userId;

    @Field("book_id")
    private Long bookId;

    @NotNull
    private LocalDateTime borrowDate;


    private LocalDateTime returnDate;

    @Builder.Default
    private LogStatus status = LogStatus.BORROWED;
}
