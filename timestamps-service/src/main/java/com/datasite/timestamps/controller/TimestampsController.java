package com.datasite.timestamps.controller;

import com.datasite.timestamps.model.TimestampRequest;
import com.datasite.timestamps.model.TimestampResponse;
import com.datasite.timestamps.service.TimestampsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/timestamps")
public class TimestampsController {

    private final TimestampsService timestampsService;

    public TimestampsController(final TimestampsService timestampsService) {
        this.timestampsService = timestampsService;
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public TimestampResponse search(@Valid @RequestBody final TimestampRequest timestampRequest) {

        return timestampsService.search(timestampRequest);
    }
}
