// src/pages/AboutPage.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import credigoLogo from '../assets/images/credigo_icon.svg';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

function AboutPage() {
  const teamMembers = [
    { name: 'Lood, Jake Luis E.', role: 'Developer', imageUrl: null },
    { name: 'Largoza, Darwin Darryle Jean E.', role: 'Developer', imageUrl: null },
    { name: 'Pajares, Josemar', role: 'Developer', imageUrl: null },
  ];

  return (
    // *** CORRECTED min-h-screen CLASS HERE ***
    <div className="flex flex-col min-h-screen font-sans">
      <Navbar />

      {/* Main content area */}
      <main className="flex-grow bg-credigo-dark text-credigo-light pt-10 pb-16 px-4">
        <div className="container mx-auto max-w-4xl">

          {/* Header Section */}
          <div className="text-center mb-12 md:mb-16">
            <img src={credigoLogo} alt="CrediGo Logo" className="h-20 w-auto mx-auto mb-4" />
            <h1 className="text-4xl md:text-5xl font-bold mb-4 text-credigo-light">About CrediGo</h1>
            <p className="text-lg md:text-xl text-gray-400 max-w-2xl mx-auto">
              Your centralized hub for seamless game top-ups and digital fund management.
            </p>
          </div>

          {/* Project Purpose Section */}
          <div className="bg-credigo-input-bg p-6 md:p-8 rounded-xl shadow-lg mb-12 md:mb-16 border border-gray-700">
            <h2 className="text-2xl md:text-3xl font-semibold mb-4 text-credigo-light">Project Purpose</h2>
            <p className="text-gray-300 mb-4">
              CrediGo simplifies the process of purchasing digital funds, game credits, and virtual currency for various online games and platforms. Instead of navigating multiple websites, users can conveniently manage and execute microtransactions all in one place.
            </p>
            <p className="text-gray-300">
              Our platform offers secure transactions, a clear transaction history, and a user-friendly digital wallet, aiming to provide a smooth and efficient buying experience.
            </p>
            {/* School Project Disclaimer */}
            <div className="mt-6 p-4 border border-yellow-500/50 bg-yellow-500/10 rounded-lg text-yellow-200 text-sm">
              <span className="font-bold">Important Note:</span> CrediGo is a project developed for academic purposes for the System Integration and Architecture 1 course at Cebu Institute of Technology - University. It is intended for demonstration and educational use only and does not process real financial transactions or provide actual game top-ups.
            </div>
          </div>

          {/* Team Section */}
          <div className="text-center">
            <h2 className="text-2xl md:text-3xl font-semibold mb-8 text-credigo-light">Our Team</h2>
            <div className="flex flex-wrap justify-center gap-8">
              {teamMembers.map((member) => (
                <div key={member.name} className="text-center">
                  <div className="w-24 h-24 md:w-32 md:h-32 mx-auto bg-gray-700 rounded-full mb-4 flex items-center justify-center text-gray-500">
                    {member.imageUrl ? (
                      <img src={member.imageUrl} alt={member.name} className="w-full h-full rounded-full object-cover" />
                    ) : (
                      <span>No Image</span>
                    )}
                  </div>
                  <h3 className="font-medium text-credigo-light">{member.name}</h3>
                  <p className="text-sm text-credigo-button">{member.role}</p>
                </div>
              ))}
            </div>
          </div>

        </div>
      </main>

      <Footer />
    </div>
  );
}

export default AboutPage;
