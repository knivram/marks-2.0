import { ClipLoader } from "react-spinners";

function LoadingScreen() {
  return (
    <div className={"flex justify-center items-center"}>
      <ClipLoader />
    </div>
  );
}

export default LoadingScreen;
