import React, { useSate } from 'react'
import { IoArrowBackOutline } from "react-icons/io5";
import { useNavigate } from 'react-router-dom';
import NotFoundImg from '../assets/images/404image.svg';

const Page404 = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-[#232946] text-[#FFFFFE] px-4">
      <img src={NotFoundImg} alt="404 Not Found" className="w-[300px] mb-6" />
      <h1 className="text-4xl font-bold mb-2">Oops!</h1>
      <p className="text-lg text-center mb-6 max-w-md">
        We couldn't find the page you were looking for.
      </p>
      <button
        onClick={() => navigate('/')}
        className="flex items-center gap-2 px-6 py-3 bg-[#fffffe] rounded-md text-sm text-[#232946] font-bold hover:bg-[#D4D8F0] transition"
      >
        <IoArrowBackOutline size={20} />
        Go Home
      </button>
    </div>
  );
};

export default Page404;
