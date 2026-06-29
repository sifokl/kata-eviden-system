package com.saifksibi.eviden.gateway.api.controller;

import com.saifksibi.eviden.gateway.application.error.dto.ErrorResponseDTO;
import com.saifksibi.eviden.gateway.application.principal.dto.JwtPrincipalResponseDTO;
import com.saifksibi.eviden.gateway.application.principal.service.CurrentPrincipalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(
        name = "Principal",
        description = "Endpoints exposing information about the authenticated JWT principal"
)
public class MeController {

    private final CurrentPrincipalService currentPrincipalService;

    public MeController(CurrentPrincipalService currentPrincipalService) {
        this.currentPrincipalService = currentPrincipalService;
    }

    @Operation(
            summary = "Get current authenticated principal",
            description = "Returns claims and roles extracted from the validated JWT.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authenticated principal returned successfully",
                            content = @Content(schema = @Schema(implementation = JwtPrincipalResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing, expired or invalid JWT token",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                    )
            }
    )
    @GetMapping("/api/me")
    public JwtPrincipalResponseDTO me() {
        return currentPrincipalService.getCurrentPrincipal();
    }
}