package com.coachdiff.infrastructure.adapter.in.rest;

public record ApiError(int status, String code, String message) {}
