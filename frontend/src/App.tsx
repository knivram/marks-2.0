import "./App.css";
import {
  createBrowserRouter,
  createRoutesFromElements,
  Navigate,
  Route,
  RouterProvider,
} from "react-router-dom";
import Dashboard from "@/routes/Dashboard.tsx";
import ErrorPage from "@/routes/ErrorPage.tsx";
import Settings from "@/routes/Settings.tsx";
import Marks from "@/routes/Marks.tsx";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster.tsx";
import ApplicationLayout from "@/ApplicationLayout.tsx";

const queryClient = new QueryClient();

const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="">
      <Route path="unknown-user" element={<ErrorPage errorCode={401} />} />
      <Route path="" element={<ApplicationLayout />}>
        <Route index element={<Navigate to="/dashboard" />} />
        <Route path="marks/:semesterId" element={<Marks />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="settings" element={<Settings />} />
        <Route path="*" element={<ErrorPage errorCode={404} />} />
      </Route>
    </Route>,
  ),
  {
    basename: "/marks",
  },
);

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <Toaster />
    </QueryClientProvider>
  );
}

export default App;
