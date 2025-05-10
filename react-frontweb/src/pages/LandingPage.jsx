import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';

function LandingPage() {
  const games = [
    { name: 'PUBG', logo: 'https://placehold.co/100x100/2a304d/ffffff?text=PUBG' },
    { name: 'Valorant', logo: 'https://placehold.co/100x100/fd4556/ffffff?text=Valorant' },
    { name: 'Fortnite', logo: 'https://placehold.co/100x100/2F80ED/ffffff?text=Fortnite' },
    { name: 'Mobile Legends', logo: 'https://placehold.co/100x100/7030A0/ffffff?text=ML' },
    { name: 'Call of Duty', logo: 'https://placehold.co/100x100/FF9900/ffffff?text=COD' },
    { name: 'Apex Legends', logo: 'https://placehold.co/100x100/E6331A/ffffff?text=Apex' }
  ];

  return (
    <div className="min-h-screen bg-credigo-dark text-credigo-light overflow-hidden">
      {/* Header Section */}
      <header className="relative z-10">
        <div className="container mx-auto px-4 py-6 flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <img src="/img/credigo_icon.svg" alt="CrediGo Logo" className="h-10 w-10" />
            <span className="text-2xl font-bold text-credigo-accent font-montserrat">CrediGo</span>
          </div>
          <div className="flex space-x-4">
            <Link to="/login" className="px-4 py-2 border border-credigo-accent text-credigo-accent rounded-md hover:bg-credigo-accent hover:text-credigo-dark transition-all">
              Login
            </Link>
            <Link to="/register" className="px-4 py-2 bg-credigo-accent text-credigo-dark font-medium rounded-md hover:bg-opacity-90 transition-all">
              Register
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="py-16 lg:py-24 relative">
        {/* Abstract shapes background */}
        <div className="absolute inset-0 overflow-hidden -z-10">
          <div className="absolute -top-20 -right-20 w-64 h-64 bg-purple-600 rounded-full filter blur-3xl opacity-20"></div>
          <div className="absolute top-40 -left-20 w-72 h-72 bg-blue-500 rounded-full filter blur-3xl opacity-20"></div>
          <div className="absolute bottom-0 right-1/4 w-80 h-80 bg-cyan-400 rounded-full filter blur-3xl opacity-10"></div>
        </div>

        <div className="container mx-auto px-4">
          <div className="flex flex-col lg:flex-row items-center gap-12">
            <div className="lg:w-1/2 space-y-8">
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6 }}
              >
                <h1 className="text-5xl lg:text-6xl font-bold leading-tight mb-4">
                  Level Up Your <span className="text-transparent bg-clip-text bg-gradient-to-r from-credigo-accent via-purple-400 to-blue-500">Gaming</span> Experience
                </h1>
                <p className="text-xl text-gray-300 mb-8">
                  Get instant game points and credits for all your favorite games. Fast, secure, and hassle-free.
                </p>
                <div className="flex flex-col sm:flex-row sm:space-x-4 space-y-3 sm:space-y-0">
                  <Link
                    to="/register"
                    className="px-8 py-4 bg-gradient-to-r from-credigo-accent to-purple-500 text-credigo-dark font-bold rounded-md hover:shadow-lg hover:shadow-purple-500/20 transition-all text-center"
                  >
                    START GAMING
                  </Link>
                  <Link
                    to="/products"
                    className="px-8 py-4 bg-credigo-input-bg border border-credigo-accent/30 text-credigo-light rounded-md hover:border-credigo-accent transition-all text-center"
                  >
                    BROWSE GAMES
                  </Link>
                </div>
              </motion.div>

              {/* Supported Games Pills */}
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.4, duration: 0.6 }}
                className="mt-10"
              >
                <p className="text-sm uppercase tracking-wider text-gray-400 mb-3">Popular Games</p>
                <div className="flex flex-wrap gap-2">
                  {games.map((game, index) => (
                    <motion.div
                      key={game.name}
                      initial={{ opacity: 0, scale: 0.8 }}
                      animate={{ opacity: 1, scale: 1 }}
                      transition={{ delay: 0.5 + index * 0.1 }}
                      className="flex items-center gap-2 bg-credigo-input-bg px-3 py-2 rounded-full"
                    >
                      <img src={game.logo} alt={game.name} className="w-6 h-6 rounded-full" />
                      <span className="text-sm font-medium">{game.name}</span>
                    </motion.div>
                  ))}
                </div>
              </motion.div>
            </div>

            <motion.div
              className="lg:w-1/2 relative"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.2, duration: 0.8 }}
            >
              <div className="relative">
                {/* 3D Gaming Device Mockup */}
                <div className="w-full h-96 bg-gradient-to-br from-blue-600/30 to-purple-600/30 rounded-2xl p-6 backdrop-blur-sm border border-white/10 shadow-xl">
                  <div className="flex flex-col h-full justify-between">
                    <div className="flex justify-between items-start">
                      <div className="p-3 bg-credigo-accent/10 rounded-lg border border-credigo-accent/20">
                        <img src="/img/credigo_icon.svg" alt="CrediGo" className="h-8 w-8" />
                      </div>

                      <div className="text-right">
                        <div className="text-sm text-gray-400">BALANCE</div>
                        <div className="text-xl font-bold text-credigo-accent">9,999 Credits</div>
                      </div>
                    </div>

                    <div className="space-y-4">
                      <div className="bg-credigo-dark/50 p-4 rounded-lg border border-white/5">
                        <div className="flex justify-between mb-2">
                          <span className="text-gray-400">PUBG Mobile</span>
                          <span className="text-credigo-accent font-bold">2,500 UC</span>
                        </div>
                        <div className="w-full bg-gray-700 h-2 rounded-full">
                          <div className="bg-gradient-to-r from-credigo-accent to-purple-500 h-2 rounded-full w-3/4"></div>
                        </div>
                      </div>

                      <div className="flex justify-between gap-4">
                        <button className="flex-1 bg-credigo-accent text-credigo-dark font-bold py-3 rounded-lg">BUY NOW</button>
                        <button className="flex-1 bg-gray-700 text-gray-200 py-3 rounded-lg">GIFT</button>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Decorative elements */}
                <div className="absolute -top-4 -right-4 w-20 h-20 bg-blue-500/30 rounded-full blur-xl"></div>
                <div className="absolute -bottom-4 -left-4 w-20 h-20 bg-purple-500/30 rounded-full blur-xl"></div>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Game Categories Section */}
      <section className="py-20 relative">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold mb-3">Top Game Categories</h2>
            <p className="text-gray-400 max-w-2xl mx-auto">
              Buy game credits, gift cards, and in-game items for your favorite games across all platforms
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              { name: 'Battle Royale', icon: '🔫', color: 'from-blue-500 to-cyan-400' },
              { name: 'MOBA', icon: '🏆', color: 'from-purple-500 to-pink-500' },
              { name: 'FPS', icon: '🎯', color: 'from-amber-500 to-red-500' },
              { name: 'RPG', icon: '⚔️', color: 'from-emerald-500 to-lime-500' }
            ].map((category, index) => (
              <motion.div
                key={category.name}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1, duration: 0.5 }}
                viewport={{ once: true }}
                className={`bg-gradient-to-br ${category.color} p-1 rounded-xl`}
              >
                <div className="bg-credigo-dark h-full p-6 rounded-lg flex flex-col items-center text-center">
                  <div className="text-4xl mb-4">{category.icon}</div>
                  <h3 className="text-xl font-bold mb-2">{category.name}</h3>
                  <p className="text-gray-400 text-sm mb-4">Top selling games in the {category.name} category</p>
                  <Link to="/products" className="text-credigo-accent hover:underline text-sm mt-auto">
                    Browse {category.name} Games →
                  </Link>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-gradient-to-b from-credigo-dark to-credigo-dark/80">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold mb-3">Why Choose CrediGo?</h2>
            <p className="text-gray-400 max-w-2xl mx-auto">
              The ultimate destination for gamers to buy game credits, gift cards and in-game items
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              {
                icon: '⚡',
                title: 'Instant Delivery',
                description: 'Get your game credits within seconds of purchase - no waiting time!'
              },
              {
                icon: '🔒',
                title: 'Secure Payments',
                description: 'Industry-leading security protocols to keep your transactions safe'
              },
              {
                icon: '💰',
                title: 'Best Prices',
                description: 'Competitive prices and regular discounts on all your favorite games'
              }
            ].map((feature, index) => (
              <motion.div
                key={feature.title}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1, duration: 0.5 }}
                viewport={{ once: true }}
                className="bg-credigo-input-bg p-6 rounded-xl border border-gray-700 hover:border-credigo-accent/30 transition-all"
              >
                <div className="text-4xl mb-4">{feature.icon}</div>
                <h3 className="text-xl font-bold mb-2">{feature.title}</h3>
                <p className="text-gray-400">
                  {feature.description}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Testimonials Section */}
      <section className="py-20 relative">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold mb-3">Gamers Love Us</h2>
            <p className="text-gray-400 max-w-2xl mx-auto">
              Join thousands of satisfied gamers who trust CrediGo for their gaming needs
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[
              {
                name: 'Alex',
                game: 'PUBG Player',
                text: 'Super fast delivery! Got my UC within seconds and was back in the game immediately. Highly recommend!'
              },
              {
                name: 'Sarah',
                game: 'Valorant Pro',
                text: 'Best prices I\'ve found for Valorant points. The secure checkout gives me peace of mind every time.'
              },
              {
                name: 'Miguel',
                game: 'Mobile Legends Player',
                text: 'I\'ve been using CrediGo for months now - never had any issues. Their customer support is amazing too!'
              }
            ].map((testimonial, index) => (
              <motion.div
                key={testimonial.name}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1, duration: 0.5 }}
                viewport={{ once: true }}
                className="bg-credigo-input-bg p-6 rounded-xl border border-gray-700"
              >
                <p className="mb-4 text-gray-300">"{testimonial.text}"</p>
                <div className="flex items-center">
                  <div className="w-10 h-10 bg-gradient-to-br from-credigo-accent to-purple-500 rounded-full flex items-center justify-center font-bold text-credigo-dark">
                    {testimonial.name.charAt(0)}
                  </div>
                  <div className="ml-3">
                    <p className="font-bold">{testimonial.name}</p>
                    <p className="text-sm text-gray-400">{testimonial.game}</p>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-credigo-accent/20 to-purple-600/20 relative">
        <div className="container mx-auto px-4 text-center">
          <div className="max-w-3xl mx-auto">
            <h2 className="text-4xl font-bold mb-6">Ready to Level Up Your Gaming?</h2>
            <p className="text-xl text-gray-300 mb-8">
              Join thousands of gamers who trust CrediGo for their in-game purchases.
              Create your account in seconds and start gaming!
            </p>
            <Link
              to="/register"
              className="inline-block px-8 py-4 bg-gradient-to-r from-credigo-accent to-purple-500 text-credigo-dark font-bold rounded-md hover:shadow-lg hover:shadow-purple-500/20 transition-all"
            >
              GET STARTED NOW
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-black/50 text-gray-400 py-10">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between">
            <div className="mb-6 md:mb-0">
              <div className="flex items-center space-x-2 mb-4">
                <img src="/img/credigo_icon.svg" alt="CrediGo Logo" className="h-8 w-8" />
                <span className="text-xl font-bold text-white font-montserrat">CrediGo</span>
              </div>
              <p className="text-sm max-w-xs">
                CrediGo is the leading provider of game credits, gift cards, and in-game items for gamers worldwide.
              </p>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-3 gap-8">
              <div>
                <h3 className="text-white font-medium mb-2">Games</h3>
                <ul className="space-y-2 text-sm">
                  <li><Link to="/products" className="hover:text-credigo-accent transition-colors">PUBG Mobile</Link></li>
                  <li><Link to="/products" className="hover:text-credigo-accent transition-colors">Valorant</Link></li>
                  <li><Link to="/products" className="hover:text-credigo-accent transition-colors">Mobile Legends</Link></li>
                </ul>
              </div>

              <div>
                <h3 className="text-white font-medium mb-2">Company</h3>
                <ul className="space-y-2 text-sm">
                  <li><Link to="/about" className="hover:text-credigo-accent transition-colors">About Us</Link></li>
                  <li><Link to="/about" className="hover:text-credigo-accent transition-colors">Careers</Link></li>
                  <li><Link to="/about" className="hover:text-credigo-accent transition-colors">Contact</Link></li>
                </ul>
              </div>

              <div>
                <h3 className="text-white font-medium mb-2">Support</h3>
                <ul className="space-y-2 text-sm">
                  <li><Link to="/about" className="hover:text-credigo-accent transition-colors">Help Center</Link></li>
                  <li><Link to="/about" className="hover:text-credigo-accent transition-colors">Terms of Service</Link></li>
                  <li><Link to="/about" className="hover:text-credigo-accent transition-colors">Privacy Policy</Link></li>
                </ul>
              </div>
            </div>
          </div>

          <div className="border-t border-gray-800 mt-10 pt-6 flex flex-col md:flex-row justify-between items-center">
            <p className="text-sm">
              © {new Date().getFullYear()} CrediGo. All rights reserved.
            </p>
            <div className="flex space-x-4 mt-4 md:mt-0">
              <a href="#" className="text-gray-400 hover:text-credigo-accent transition-colors">
                <span className="sr-only">Discord</span>
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028c.462-.63.874-1.295 1.226-1.994a.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/>
                </svg>
              </a>
              <a href="#" className="text-gray-400 hover:text-credigo-accent transition-colors">
                <span className="sr-only">Twitter</span>
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M8.29 20.251c7.547 0 11.675-6.253 11.675-11.675 0-.178 0-.355-.012-.53A8.348 8.348 0 0022 5.92a8.19 8.19 0 01-2.357.646 4.118 4.118 0 001.804-2.27 8.224 8.224 0 01-2.605.996 4.107 4.107 0 00-6.993 3.743 11.65 11.65 0 01-8.457-4.287 4.106 4.106 0 001.27 5.477A4.072 4.072 0 012.8 9.713v.052a4.105 4.105 0 003.292 4.022 4.095 4.095 0 01-1.853.07 4.108 4.108 0 003.834 2.85A8.233 8.233 0 012 18.407a11.616 11.616 0 006.29 1.84" />
                </svg>
              </a>
              <a href="#" className="text-gray-400 hover:text-credigo-accent transition-colors">
                <span className="sr-only">Instagram</span>
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path fillRule="evenodd" d="M12.315 2c2.43 0 2.784.013 3.808.06 1.064.049 1.791.218 2.427.465a4.902 4.902 0 011.772 1.153 4.902 4.902 0 011.153 1.772c.247.636.416 1.363.465 2.427.048 1.067.06 1.407.06 4.123v.08c0 2.643-.012 2.987-.06 4.043-.049 1.064-.218 1.791-.465 2.427a4.902 4.902 0 01-1.153 1.772 4.902 4.902 0 01-1.772 1.153c-.636.247-1.363.416-2.427.465-1.067.048-1.407.06-4.123.06h-.08c-2.643 0-2.987-.012-4.043-.06-1.064-.049-1.791-.218-2.427-.465a4.902 4.902 0 01-1.772-1.153 4.902 4.902 0 01-1.153-1.772c-.247-.636-.416-1.363-.465-2.427-.047-1.024-.06-1.379-.06-3.808v-.63c0-2.43.013-2.784.06-3.808.049-1.064.218-1.791.465-2.427a4.902 4.902 0 011.153-1.772A4.902 4.902 0 015.45 2.525c.636-.247 1.363-.416 2.427-.465C8.901 2.013 9.256 2 11.685 2h.63zm-.081 1.802h-.468c-2.456 0-2.784.011-3.807.058-.975.045-1.504.207-1.857.344-.467.182-.8.398-1.15.748-.35.35-.566.683-.748 1.15-.137.353-.3.882-.344 1.857-.047 1.023-.058 1.351-.058 3.807v.468c0 2.456.011 2.784.058 3.807.045.975.207 1.504.344 1.857.182.466.399.8.748 1.15.35.35.683.566 1.15.748.353.137.882.3 1.857.344 1.054.048 1.37.058 4.041.058h.08c2.597 0 2.917-.01 3.96-.058.976-.045 1.505-.207 1.858-.344.466-.182.8-.398 1.15-.748.35-.35.566-.683.748-1.15.137-.353.3-.882.344-1.857.048-1.055.058-1.37.058-4.041v-.08c0-2.597-.01-2.917-.058-3.96-.045-.976-.207-1.505-.344-1.858a3.097 3.097 0 00-.748-1.15 3.098 3.098 0 00-1.15-.748c-.353-.137-.882-.3-1.857-.344-1.023-.047-1.351-.058-3.807-.058zM12 6.865a5.135 5.135 0 110 10.27 5.135 5.135 0 010-10.27zm0 1.802a3.333 3.333 0 100 6.666 3.333 3.333 0 000-6.666zm5.338-3.205a1.2 1.2 0 110 2.4 1.2 1.2 0 010-2.4z" clipRule="evenodd" />
                </svg>
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default LandingPage;
