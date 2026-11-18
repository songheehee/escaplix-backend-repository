package com.escaplix.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** HOLIDAY 공휴일 (C). 날짜가 PK. 공휴일은 주말 시간표 (DayTypeResolver) */
@Entity
@Table(name = "holiday")
@Getter
@Setter
@NoArgsConstructor
public class Holiday {

    @Id
    @Column(name = "holiday_date")
    private LocalDate holidayDate;

    @Column(nullable = false, length = 60)
    private String name;
}
