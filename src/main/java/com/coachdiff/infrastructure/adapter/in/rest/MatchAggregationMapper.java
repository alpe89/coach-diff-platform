package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.model.MatchAggregate;
import com.coachdiff.infrastructure.adapter.in.rest.dto.MatchAggregationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MatchAggregationMapper {
  @Mapping(target = "winRate", expression = "java(matchAggregation.winRate())")

  MatchAggregationDto toDto(MatchAggregate matchAggregation);
}
