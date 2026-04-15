import { Routes } from "@angular/router";
import { AuthLayoutComponent } from "./layouts/auth-layout/auth-layout.component";
import { MainLayoutComponent } from "./layouts/main-layout/main-layout.component";
import { LoginComponent } from "./components/login/login.component";
import { RegisterComponent } from "./components/register/register.component";
// Guards commented out for testing
// import { AuthGuard } from "./core/guards/auth.guard";
// import { UnauthGuard } from "./core/guards/unauth.guard";
// import { RoleGuard } from "./core/guards/role.guard";

export const routes: Routes = [
  {
    path: "",
    component: AuthLayoutComponent,
    children: [
      {
        path: "login",
        component: LoginComponent,
      },
      {
        path: "register",
        component: RegisterComponent,
      },
    ],
  },
  {
    path: "",
    component: MainLayoutComponent,
    // canActivate: [AuthGuard], // Removed for testing
    children: [
      {
        path: "dashboard",
        loadComponent: () =>
            import("./components/dashboard/dashboard.component").then(
                (m) => m.DashboardComponent
            ),
        // canActivate: [RoleGuard],
        // data: { roles: ["ROLE_ADMIN"] }
      },
      {
        path: "test-auth",
        loadComponent: () =>
            import("./components/test-auth/test-auth.component").then(
                (m) => m.TestAuthComponent
            ),
        // canActivate: [RoleGuard],
        // data: { roles: ["ROLE_ADMIN"] }
      },
      {
        path: "admin",
        loadComponent: () =>
            import("./components/admin/admin.component").then(
                (m) => m.AdminComponent
            ),
        // canActivate: [RoleGuard],
        // data: { roles: ["ROLE_ADMIN"] }
      },
      // User Management Routes (Standalone)
      {
        path: "users",
        children: [
          {
            path: "",
            redirectTo: "list",
            pathMatch: "full"
          },
          {
            path: "list",
            loadComponent: () =>
              import("./components/user-management/user-list/user-list.component").then(
                (m) => m.UserListComponent
              ),
            data: {
              title: "User Management",
              breadcrumb: "Users"
            }
          },
          {
            path: "create",
            loadComponent: () =>
              import("./components/user-management/user-form/user-form.component").then(
                (m) => m.UserFormComponent
              ),
            data: {
              title: "Create User",
              breadcrumb: "Create"
            }
          },
          {
            path: "edit/:id",
            loadComponent: () =>
              import("./components/user-management/user-form/user-form.component").then(
                (m) => m.UserFormComponent
              ),
            data: {
              title: "Edit User",
              breadcrumb: "Edit"
            }
          },
          {
            path: ":id",
            loadComponent: () =>
              import("./components/user-management/user-detail/user-detail.component").then(
                (m) => m.UserDetailComponent
              ),
            data: {
              title: "User Details",
              breadcrumb: "Details"
            }
          }
        ]
        // canActivate: [RoleGuard],
        // data: { roles: ["ROLE_ADMIN"] }
      },
      // Subject Management Routes (Standalone)
      {
        path: "subjects",
        children: [
          {
            path: "",
            redirectTo: "list",
            pathMatch: "full"
          },
          {
            path: "list",
            loadComponent: () =>
              import("./components/subject-management/subject-list/subject-list.component").then(
                (m) => m.SubjectListComponent
              ),
            data: {
              title: "Subject Management",
              breadcrumb: "Subjects"
            }
          }
        ]
        // canActivate: [RoleGuard],
        // data: { roles: ["ROLE_ADMIN"] }
      },
      // Tutor Search Routes (Standalone)
      {
        path: "tutors",
        children: [
          {
            path: "",
            redirectTo: "search",
            pathMatch: "full"
          },
          {
            path: "search",
            loadComponent: () =>
              import("./components/tutor-search/tutor-search-list/tutor-search-list.component").then(
                (m) => m.TutorSearchListComponent
              ),
            data: {
              title: "Find Tutors",
              breadcrumb: "Search"
            }
          }
        ]
        // canActivate: [RoleGuard],
        // data: { roles: ["ROLE_STUDENT"] }
      },
      // Session Management Routes (Standalone)
      {
        path: "sessions",
        children: [
          {
            path: "",
            redirectTo: "list",
            pathMatch: "full"
          },
          {
            path: "list",
            loadComponent: () =>
              import("./components/session/session-list/session-list.component").then(
                (m) => m.SessionListComponent
              ),
            data: {
              title: "My Sessions",
              breadcrumb: "Sessions"
            }
          },
          {
            path: "book",
            loadComponent: () =>
              import("./components/session/session-booking/session-booking.component").then(
                (m) => m.SessionBookingComponent
              ),
            data: {
              title: "Book a Session",
              breadcrumb: "Book"
            }
          },
          {
            path: "book/:tutorId",
            loadComponent: () =>
              import("./components/session/session-booking/session-booking.component").then(
                (m) => m.SessionBookingComponent
              ),
            data: {
              title: "Book with Tutor",
              breadcrumb: "Book"
            }
          },
          {
            path: ":id",
            loadComponent: () =>
              import("./components/session/session-details/session-details.component").then(
                (m) => m.SessionDetailsComponent
              ),
            data: {
              title: "Session Details",
              breadcrumb: "Details"
            }
          }
        ]
        // canActivate: [RoleGuard],
        // data: { roles: ["ROLE_STUDENT", "ROLE_TUTOR"] }
      },
      // Session Requests Routes (Standalone)
      {
        path: "session-requests",
        children: [
          {
            path: "",
            redirectTo: "list",
            pathMatch: "full"
          },
          {
            path: "list",
            loadComponent: () =>
              import("./components/session/session-list/session-list.component").then(
                (m) => m.SessionListComponent
              ),
            data: {
              title: "Session Requests",
              breadcrumb: "Requests",
              viewType: "requests"
            }
          },
          {
            path: ":id",
            loadComponent: () =>
              import("./components/session/session-details/session-details.component").then(
                (m) => m.SessionDetailsComponent
              ),
            data: {
              title: "Request Details",
              breadcrumb: "Details",
              viewType: "request"
            }
          }
        ]
        // canActivate: [RoleGuard],
        // data: { roles: ["ROLE_STUDENT", "ROLE_TUTOR"] }
      },
      {
        path: "",
        redirectTo: "dashboard",
        pathMatch: "full",
      },
    ],
  },
  {
    path: "access-denied",
    loadComponent: () => import("./components/access-denied/access-denied.component").then(m => m.AccessDeniedComponent)
  },
  { path: "**", redirectTo: "/login" },
];
