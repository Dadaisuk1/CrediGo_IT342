// src/components/Footer.jsx
import React from 'react';
import { Link } from 'react-router-dom'; // Assuming you use React Router for internal links
import { Facebook, Instagram, Twitter, Github, Youtube } from 'lucide-react'; // Import icons

function Footer() {
  const year = new Date().getFullYear(); // Get current year for copyright

  // Placeholder links - replace with actual paths or URLs
  const footerLinks = [
    { name: 'About', href: '/about' },
    { name: 'Blog', href: '/blog' },
    { name: 'Jobs', href: '/jobs' },
    { name: 'Press', href: '/press' },
    { name: 'Accessibility', href: '/accessibility' },
    { name: 'Partners', href: '/partners' },
  ];

  const socialLinks = [
    { name: 'Facebook', href: '#', icon: Facebook },
    { name: 'Instagram', href: '#', icon: Instagram },
    { name: 'Twitter', href: '#', icon: Twitter }, // Or use X icon if preferred
    { name: 'GitHub', href: 'https://github.com/Dadaisuk1/CrediGo_IT342', icon: Github }, // Link to your repo
    { name: 'YouTube', href: '#', icon: Youtube },
  ];

  return (
    // Added border-t and border-gray-700 to the footer element
    <footer className="bg-credigo-dark text-gray-400 font-sans mt-auto border-t border-gray-700"> {/* Use dark bg, light text, ensure it sticks to bottom if needed */}
      <div className="container mx-auto px-4 py-8 md:py-12">
        {/* Top Links */}
        <nav className="flex flex-wrap justify-center gap-x-6 gap-y-2 mb-8">
          {footerLinks.map((item) => (
            <Link // Use Link for internal routes, <a> for external
              key={item.name}
              to={item.href} // Use 'to' for React Router Link
              className="text-sm hover:text-credigo-light transition duration-150"
            >
              {item.name}
            </Link>
          ))}
        </nav>

        {/* Social Icons */}
        <div className="flex justify-center space-x-6 mb-8">
          {socialLinks.map((item) => (
            <a // Use <a> for external social links
              key={item.name}
              href={item.href}
              target="_blank" // Open in new tab
              rel="noopener noreferrer" // Security best practice
              className="text-gray-400 hover:text-credigo-light transition duration-150"
              aria-label={item.name} // Accessibility
            >
              <item.icon className="h-6 w-6" /> {/* Render the icon component */}
            </a>
          ))}
        </div>

        {/* Copyright */}
        <div className="text-center text-sm">
          &copy; {year} CrediGo Team (Lood, Largoza, Pajares). All rights reserved.
        </div>
      </div>
    </footer>
  );
}

export default Footer;
