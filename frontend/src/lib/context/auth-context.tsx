import { createContext, PropsWithChildren, useContext, useEffect } from "react";
import { User } from "@/api/user/user.types.ts";
import { useGetLoggedInUser } from "@/api/user/user.api.ts";
import { useNavigate } from "react-router-dom";
import LoadingScreen from "@/components/LoadingScreen.tsx";

interface AuthContextValue {
  user: User;
}

const AuthContext = createContext<AuthContextValue>(null!);

export const AuthContextProvider = ({ children }: PropsWithChildren) => {
  const { data: user, isLoading, isError } = useGetLoggedInUser();
  const navigate = useNavigate();

  useEffect(() => {
    if (isError || (!user && !isLoading)) {
      navigate("/unknown-user");
    }
  }, [user, isLoading, isError, navigate]);

  if (isLoading) {
    return <LoadingScreen />;
  }

  return (
    <AuthContext.Provider value={{ user: user! }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useReadAuthContext = () => useContext(AuthContext);
