import sys, os
out = sys.argv[1]
P = 'M62 6L16 58H44L34 94L84 38H57L70 6Z'
S = 'fill="none" stroke="#000" stroke-width="3.6" stroke-linecap="round"'
dots = lambda dx, dy: f'<circle cx="{41+dx}" cy="{47+dy}" r="3.8"/><circle cx="{58+dx}" cy="{45+dy}" r="3.8"/>'
EYE = {'open': dots(0,0), 'look': dots(2,-1.5),
       'happy': f'<path d="M36.5 48.5Q41 43 45.5 48.5M53.5 46.5Q58 41 62.5 46.5" {S}/>'}
MOUTH = {'smile': [44,55.5,51,61.5,58,53.5], 'big': [42.5,54,51,66,60,52], 'small': [47.5,56.5,51,59,54.5,56],
         'focus': [46,56,51,58.5,56,55.5], 'worry': [45,59,51,54,57,58]}
X = {'rest': ('open','smile'), 'happy': ('happy','big'), 'focus': ('open','focus'), 'waiting': ('look','focus'),
     'curious': ('open','small'), 'oops': ('open','worry')}
def mark(state, fg, bg=None, size=120):
    e, m = X[state]; q = MOUTH[m]
    mouth = f'<path d="M{q[0]} {q[1]}Q{q[2]} {q[3]} {q[4]} {q[5]}" {S}/>'
    tilt = ' transform="rotate(-9 50 50)"' if state == 'curious' else ''
    back = f'<rect x="-10" y="-10" width="120" height="120" fill="{bg}"/>' if bg else ''
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="-10 -10 120 120" width="{size}" height="{size}">
  <title>Zap of Nakamoto – {state}</title>
  <defs><mask id="face" maskUnits="userSpaceOnUse" x="-20" y="-20" width="140" height="140">
    <rect x="-20" y="-20" width="140" height="140" fill="#fff"/>{EYE[e]}{mouth}
  </mask></defs>
  {back}<g{tilt}><path d="{P}" fill="{fg}" stroke="{fg}" stroke-width="12" stroke-linejoin="round" mask="url(#face)"/></g>
</svg>
'''
w = lambda p, s: open(os.path.join(out, p), 'w').write(s)
w('mark-ink.svg', mark('rest', '#16182B'))
w('mark-spark.svg', mark('rest', '#FFD23F'))
w('app-icon.svg', mark('rest', '#16182B', '#FFD23F', 512))
for st in X: w(f'states/{st}.svg', mark(st, '#16182B'))
