export default function SectionHeading({ eyebrow, title, subtitle, light = false, center = true }) {
  return (
    <div className={center ? 'text-center' : ''}>
      {eyebrow && (
        <p
          className={`text-xs font-bold uppercase tracking-[0.2em] ${
            light ? 'text-gold-400' : 'text-gold-600'
          }`}
        >
          {eyebrow}
        </p>
      )}
      <h2
        className={`mt-2 font-display text-3xl font-bold sm:text-4xl ${
          light ? 'text-white' : 'text-navy-950'
        }`}
      >
        {title}
      </h2>
      {subtitle && (
        <p
          className={`mt-3 max-w-2xl text-base leading-relaxed ${
            light ? 'text-white/70' : 'text-navy-500'
          } ${center ? 'mx-auto' : ''}`}
        >
          {subtitle}
        </p>
      )}
    </div>
  )
}
