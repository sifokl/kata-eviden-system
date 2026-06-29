package com.saifksibi.eviden.gateway.api.controller;

import com.saifksibi.eviden.gateway.application.error.dto.ErrorResponseDTO;
import com.saifksibi.eviden.gateway.application.proxy.dto.BackendResourceResponseDTO;
import com.saifksibi.eviden.gateway.application.proxy.service.ProxyBackendService;
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
        name = "Books proxy",
        description = "Gateway endpoints exposing public, member and admin book resources"
)
public class ResourceProxyController {

    private final ProxyBackendService proxyBackendService;

    public ResourceProxyController(ProxyBackendService proxyBackendService) {
        this.proxyBackendService = proxyBackendService;
    }

    @Operation(
            summary = "Get public books",
            description = "Public endpoint. The gateway proxies the request to the public books resource without requiring a JWT.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Public books returned successfully",
                            content = @Content(schema = @Schema(implementation = BackendResourceResponseDTO.class))
                    )
            }
    )
    @GetMapping("/public/books")
    public BackendResourceResponseDTO getPublicBooks() {
        return proxyBackendService.getPublicBooks();
    }

    @Operation(
            summary = "Get member books",
            description = """
                    Protected endpoint. Requires a valid JWT containing either USER or ADMIN role.
                    The gateway rewrites /members/books to the configured backend members resource path.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Member books returned successfully",
                            content = @Content(schema = @Schema(implementation = BackendResourceResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing, expired or invalid JWT token",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Authenticated principal does not have USER or ADMIN role",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                    )
            }
    )
    @GetMapping("/members/books")
    public BackendResourceResponseDTO getMembersBooks() {
        return proxyBackendService.getMembersBooks();
    }

    @Operation(
            summary = "Get admin books",
            description = """
                    Admin-only endpoint. Requires a valid JWT containing ADMIN role.
                    The gateway rewrites /admin/books to the configured backend admin resource path.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Admin books returned successfully",
                            content = @Content(schema = @Schema(implementation = BackendResourceResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing, expired or invalid JWT token",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Authenticated principal does not have ADMIN role",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                    )
            }
    )
    @GetMapping("/admin/books")
    public BackendResourceResponseDTO getAdminBooks() {
        return proxyBackendService.getAdminBooks();
    }
}