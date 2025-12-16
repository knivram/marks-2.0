import { AuthContextProvider } from "@/lib/context/auth-context.tsx";
import { Outlet } from "react-router-dom";
import { ApprenticeContextProvider } from "@/lib/context/apprentice-context.tsx";

function ApplicationLayout() {
  return (
    <>
      <AuthContextProvider>
        <ApprenticeContextProvider>
          <Outlet />
        </ApprenticeContextProvider>
      </AuthContextProvider>
    </>
  );
}

export default ApplicationLayout;
