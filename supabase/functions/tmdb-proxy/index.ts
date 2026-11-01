import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const TMDB_BASE_URL = 'https://api.themoviedb.org/3'
const TMDB_READ_ACCESS_TOKEN = Deno.env.get('TMDB_READ_ACCESS_TOKEN')

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    if (!TMDB_READ_ACCESS_TOKEN) {
      throw new Error('TMDB_READ_ACCESS_TOKEN is not set in Edge Function secrets.')
    }

    const { endpoint, params = {} } = await req.json()

    if (!endpoint) {
      return new Response(
        JSON.stringify({ error: 'Endpoint is required' }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Construct URL with query parameters
    const url = new URL(`${TMDB_BASE_URL}/${endpoint}`)
    Object.keys(params).forEach(key => url.searchParams.append(key, params[key]))

    console.log(`Proxying request to TMDB: ${url.toString()}`)

    const response = await fetch(url.toString(), {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${TMDB_READ_ACCESS_TOKEN}`,
        'accept': 'application/json',
      },
    })

    const data = await response.json()

    return new Response(
      JSON.stringify(data),
      { 
        status: response.status, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    )
  } catch (error) {
    console.error(`Error in TMDB proxy: ${error.message}`)
    return new Response(
      JSON.stringify({ error: error.message }),
      { 
        status: 500, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    )
  }
})
