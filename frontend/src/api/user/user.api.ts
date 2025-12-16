import { useApiQuery } from "@/lib/api/useApiQuery.ts";
import { User } from "@/api/user/user.types.ts";
import { useReadAuthContext } from "@/lib/context/auth-context.tsx";

export const useGetLoggedInUser = () =>
  useApiQuery<User>({
    url: "api/user/info",
    method: "GET",
    queryConfig: {
      doNotProcessError: true,
    },
  });

export const useGetApprentice = () => {
  const { user } = useReadAuthContext();
  return useApiQuery<User[]>({
    url: `api/user/${user.userId}/apprentice`,
    method: "GET",
  });
};
