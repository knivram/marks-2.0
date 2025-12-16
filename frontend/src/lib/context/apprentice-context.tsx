import { User, UserRole } from "@/api/user/user.types.ts";
import {
  createContext,
  PropsWithChildren,
  useContext,
  useEffect,
  useState,
} from "react";
import { useGetApprentice } from "@/api/user/user.api.ts";
import { useReadAuthContext } from "@/lib/context/auth-context.tsx";
import { useToast } from "@/components/ui/use-toast.ts";
import LoadingScreen from "@/components/LoadingScreen.tsx";

interface UpdateApprenticeContextValue {
  setSelectedApprentice: (apprentice: User) => void;
  setApprentices: (apprentices: User[]) => void;
}

const UpdateApprenticeContext = createContext<UpdateApprenticeContextValue>(
  null!,
);

interface ReadApprenticeContextValue {
  apprentices: User[];
  selectedApprentice: User;
}

const ReadApprenticeContext = createContext<ReadApprenticeContextValue>(null!);

export const ApprenticeContextProvider = ({ children }: PropsWithChildren) => {
  const { user } = useReadAuthContext();
  const [selectedApprentice, setSelectedApprentice] = useState<
    User | undefined
  >(user.role === UserRole.APPRENTICE ? user : undefined);
  const [apprentices, setApprentices] = useState<User[] | undefined>(
    user.role === UserRole.APPRENTICE ? [] : undefined,
  );

  if (selectedApprentice && apprentices) {
    return (
      <ReadApprenticeContext.Provider
        value={{
          selectedApprentice,
          apprentices,
        }}
      >
        <UpdateApprenticeContext.Provider
          value={{ setSelectedApprentice, setApprentices }}
        >
          {children}
        </UpdateApprenticeContext.Provider>
      </ReadApprenticeContext.Provider>
    );
  }

  return (
    <ApprenticeLoader
      setSelectedApprentice={setSelectedApprentice}
      setApprentices={setApprentices}
    />
  );
};
const ApprenticeLoader = ({
  setSelectedApprentice,
  setApprentices,
}: UpdateApprenticeContextValue) => {
  const { toast } = useToast();
  const { data, isLoading, isError } = useGetApprentice();

  useEffect(() => {
    if (isError || (!data && !isLoading)) {
      // TODO error page
      console.log("there was an error while loading you apprentices.");
    } else if (data) {
      if (data.length > 0) {
        setApprentices(data);
        setSelectedApprentice(data[0]);
      } else {
        // TODO error page
        console.log("you dont have any apprentice pleas contact bernd.");
      }
    }
  }, [data, isLoading, isError, toast, setSelectedApprentice, setApprentices]);

  return <LoadingScreen />;
};

export const useReadApprenticeContext = () => useContext(ReadApprenticeContext);

export const useUpdateApprenticeContext = () =>
  useContext(UpdateApprenticeContext);
