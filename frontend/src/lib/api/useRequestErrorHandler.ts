import { handleErrorCode } from "@/lib/api/error.types.ts";
import { useToast } from "@/components/ui/use-toast.ts";
import { useCallback } from "react";

export const useRequestErrorHandler = () => {
  const { toast } = useToast();
  const handleError = useCallback(
    (requestError: Error) => {
      const errorCode = handleErrorCode(requestError);

      if (errorCode?.toString().startsWith("4")) {
        toast({
          title: "Oops! Etwas ist schiefgelaufen.",
          description:
            "Ihre Anfrage konnte nicht verarbeitet werden. Überprüfen Sie Ihre Eingaben und versuchen Sie es erneut.",
          variant: "destructive",
        });
      } else if (errorCode?.toString().startsWith("5")) {
        toast({
          title: "Oops! Etwas ist schiefgelaufen.",
          description:
            "Es gab ein Problem auf unserem Server. Bitte versuchen Sie es später erneut.",
          variant: "destructive",
        });
      } else {
        toast({
          title: "Unbekannter Fehler",
          description:
            "Ein unerwarteter Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.",
          variant: "destructive",
        });
      }
    },
    [toast],
  );
  return { handleError };
};
