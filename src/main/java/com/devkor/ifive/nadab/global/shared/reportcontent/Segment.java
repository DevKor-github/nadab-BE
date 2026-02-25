package com.devkor.ifive.nadab.global.shared.reportcontent;

import java.util.List;

public record Segment(
        String text,
        List<Mark> marks
) { }
