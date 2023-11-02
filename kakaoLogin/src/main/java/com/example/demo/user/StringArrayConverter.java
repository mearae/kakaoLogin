package com.example.demo.user;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StringArrayConverter implements AttributeConverter<List<String>, String> {
    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        // ** 전달받은 리스트에 있는 구성요소들을 ","로 구분하여 하나의 문자열로 변환
        return attribute.stream().map(String::valueOf).collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        // ** JPA가 save할 때, select부터 진행하기 때문에 같이 null인지 확인
        if (dbData == null){
            return Collections.emptyList();
        }
        else {
            // ** 전달받은 하나의 문자열을 ","로 구분하여 리스트로 변환
            return Arrays.stream(dbData.split(SPLIT_CHAR))
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }
    }
}
