#include <engine/shared/config.h>
#include <game/client/gameclient.h>
#include <game/client/components/controls.h>
#include "fujix_ai.h"

void CFujixAI::OnInit()
{
}

void CFujixAI::OnRender()
{
	if(!g_Config.m_ClFujixEnable)
		return;

	if(Client()->State() != IClient::STATE_ONLINE)
		return;

	UpdateControls();
}

void CFujixAI::UpdateControls()
{
	CControls *pControls = &GameClient()->m_Controls;
	int Dummy = g_Config.m_ClDummy;

	// Simple AI: Move right and jump occasionally
	pControls->m_aInputData[Dummy].m_Direction = 1;

	static int s_Tick = 0;
	s_Tick++;
	if(s_Tick % 60 < 10)
		pControls->m_aInputData[Dummy].m_Jump = 1;
	else
		pControls->m_aInputData[Dummy].m_Jump = 0;
}
