package org.oppexai.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.oppexai.dto.request.LoginRequest;
import org.oppexai.dto.request.SignUpRequest;
import org.oppexai.dto.response.AuthResponse;
import org.oppexai.dto.response.MessageResponse;
import org.oppexai.model.User;
import org.oppexai.service.AuthService;
import org.oppexai.service.UserService;


@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    UserService userService;

    @Inject
    AuthService authService;

    /**
     * POST /api/auth/signup
     * Register a new user
     */
    @POST
    @Path("/signup")
    public Response signup(@Valid SignUpRequest request) {
        try {
            LOG.infof("Signup request received for: %s", request.getEmail());

            userService.signup(request.getEmail(), request.getPassword());

            return Response.status(Response.Status.CREATED)
                    .entity(MessageResponse.success(
                            "Signup successful! Please check your email to verify your account."
                    ))
                    .build();

        } catch (BadRequestException e) {
            LOG.warnf("Signup failed: %s", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(MessageResponse.error(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOG.errorf("Signup error: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageResponse.error("Signup failed. Please try again."))
                    .build();
        }
    }


    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        try {
            LOG.infof("Login request received for: %s", request.getEmail());

            // Authenticate and get JWT token
            String token = authService.login(request.getEmail(), request.getPassword());

            // Get user details
            User user = userService.findByEmail(request.getEmail());

            // Create response
            AuthResponse response = new AuthResponse(
                    token,
                    user.getEmail(),
                    user.getIsVerified(),
                    "Login successful"
            );

            return Response.ok(response).build();

        } catch (NotAuthorizedException e) {
            LOG.warnf("Login failed: %s", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(MessageResponse.error("Invalid email or password"))
                    .build();
        } catch (Exception e) {
            LOG.errorf("Login error: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageResponse.error("Login failed. Please try again."))
                    .build();
        }
    }

    @GET
    @Path("/verify")
    @Produces(MediaType.TEXT_HTML)
    public Response verifyEmail(@QueryParam("token") String token) {
        if (token == null || token.isEmpty()) {
            return Response.seeOther(java.net.URI.create("https://oppenxai-auth-service.vercel.app/login?error=invalid_token")).build();
        }

        try {
            LOG.infof("Email verification request for token: %s", token);

            userService.verifyEmail(token);

            return Response.seeOther(java.net.URI.create("https://oppenxai-auth-service.vercel.app/dashboard")).build();

        } catch (Exception e) {
            LOG.errorf("Verification error: %s", e.getMessage());
            return Response.seeOther(java.net.URI.create("https://oppenxai-auth-service.vercel.app/login?error=verification_failed")).build();
        }
    }

    private String buildHtmlResponse(String title, String message, boolean success) {
        String color = success ? "#10b981" : "#ef4444";
        String icon = success ? "✓" : "✗";
        String buttonUrl = success ? "http://localhost:5173/dashboard" : "http://localhost:5173/signup";
        String buttonText = success ? "Go To Dashboard" : "Back to Signup";

        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>%s - Oppex AI</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                    background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    padding: 20px;
                }
                .container {
                    background: white;
                    border-radius: 16px;
                    padding: 48px;
                    max-width: 500px;
                    width: 100%%;
                    box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                    text-align: center;
                }
                .icon {
                    width: 80px;
                    height: 80px;
                    border-radius: 50%%;
                    background: %s;
                    color: white;
                    font-size: 48px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin: 0 auto 24px;
                    animation: scaleIn 0.3s ease-out;
                }
                h1 {
                    color: #1f2937;
                    font-size: 28px;
                    margin-bottom: 16px;
                }
                p {
                    color: #6b7280;
                    font-size: 16px;
                    line-height: 1.6;
                    margin-bottom: 32px;
                }
                .btn {
                    display: inline-block;
                    background: #667eea;
                    color: white;
                    padding: 12px 32px;
                    border-radius: 8px;
                    text-decoration: none;
                    font-weight: 600;
                    transition: all 0.2s;
                }
                .btn:hover {
                    background: #5568d3;
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
                }
                @keyframes scaleIn {
                    from { transform: scale(0); }
                    to { transform: scale(1); }
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="icon">%s</div>
                <h1>%s</h1>
                <p>%s</p>
                <a href="%s" class="btn">%s</a>
            </div>
        </body>
        </html>
        """,
                title,
                color,
                icon,
                title,
                message,
                buttonUrl,
                buttonText
        );
    }

    @POST
    @Path("/logout")
    public Response logout() {
        return Response.ok()
                .entity(MessageResponse.success("Logged out successfully"))
                .build();
    }
}